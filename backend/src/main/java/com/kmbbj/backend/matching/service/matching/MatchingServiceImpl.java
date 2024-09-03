package com.kmbbj.backend.matching.service.matching;

import com.kmbbj.backend.auth.entity.User;
import com.kmbbj.backend.balance.entity.TotalBalance;
import com.kmbbj.backend.balance.service.BalanceService;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import com.kmbbj.backend.global.config.security.FindUserBySecurity;
import com.kmbbj.backend.global.sse.SseService;
import com.kmbbj.backend.matching.dto.CreateRoomDTO;
import com.kmbbj.backend.matching.entity.Room;
import com.kmbbj.backend.matching.entity.StartSeedMoney;
import com.kmbbj.backend.matching.service.queue.MatchingQueueService;
import com.kmbbj.backend.matching.service.room.RoomService;
import com.kmbbj.backend.matching.service.userroom.UserRoomService;
import com.kmbbj.backend.matching.util.AssetRangeCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class MatchingServiceImpl implements MatchingService{

    private final TaskScheduler taskScheduler;
    private final Map<Long, ScheduledFuture<?>> matchingTasks = new ConcurrentHashMap<>();
    private final Map<Object, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    private final RedisTemplate<String, Object> redisTemplate;
    private final RoomService roomService;
    private final UserRoomService userRoomService;
    private final MatchingQueueService matchingQueueService;
    private final FindUserBySecurity findUserBySecurity;
    private final BalanceService balanceService;
    private final SseService sseService;
    private final AssetRangeCalculator rangeCalculator;
    private final AtomicInteger time = new AtomicInteger(0);

    @Override
    @Transactional
    public void startQuickMatching() {
        User user = findUserBySecurity.getCurrentUser();
        scheduleMatchingTasks(user,true);
    }


    @Override
    @Transactional
    public void startRandomMatching() {
        if (userRoomService.findCurrentRoom() != null) {
            throw new ApiException(ExceptionEnum.IN_OTHER_ROOM);
        }

        // 현재 유저
        User user = findUserBySecurity.getCurrentUser();

        // 현재 유저의 자산
        TotalBalance totalBalance = balanceService.totalBalanceFindByUserId(user.getId()).orElse(null);

        // 자산을 찾을 수 없는 경우 매칭 취소
        if (totalBalance == null) {
            cancelCurrentUserScheduledTasks();
            throw new ApiException(ExceptionEnum.BALANCE_NOT_FOUND);
        }


        // 매칭큐에 현재 유저 넣음
        matchingQueueService.addUserToQueue(user);
        scheduleMatchingTasks(user,false);
    }

    /**
     *
     * @param user
     */
    @Override
    @Transactional
    public void scheduleMatchingTasks(User user,boolean isQuickMatch) {
        AtomicBoolean isThirtyMinutesPassed = new AtomicBoolean(false);
        AtomicBoolean isFiveMinutesPassed = new AtomicBoolean(false);
        // 현재 SecurityContext 를 저장
        SecurityContext context = SecurityContextHolder.getContext();

        // 매칭 실행 스케줄링 (3초마다)
        ScheduledFuture<?> matchingTask = taskScheduler.scheduleWithFixedDelay(() -> {

            // 작업 스레드에 SecurityContext 설정
            SecurityContextHolder.setContext(context);
            try {
                if (isQuickMatch) {
                    handleQuickMatch(user);
                } else {
                    handleRandomMatch(user, isFiveMinutesPassed, isThirtyMinutesPassed);
                }

            } finally {
                // SecurityContext 정리
                SecurityContextHolder.clearContext();
            }
        }, 3000);
        scheduledTasks.put(user.getId(), matchingTask);
        matchingTasks.put(user.getId(), matchingTask);

        // 시간 조건 설정 (30분)
        ScheduledFuture<?> thirtyMinuteTask = taskScheduler.schedule(() -> {
            // 작업 스레드에 SecurityContext 설정
            SecurityContextHolder.setContext(context);
            try {
                isThirtyMinutesPassed.set(true);
                if (!isQuickMatch) switchToQuickMatch(user);
            } finally {
                // SecurityContext 정리
                SecurityContextHolder.clearContext();
            }
        }, new Date(System.currentTimeMillis() + Duration.ofSeconds(30).toMillis())); // 분으로 바꿔야댐
        scheduledTasks.put(user.getId(), thirtyMinuteTask);

        // 시간 조건 설정 (5분)
        ScheduledFuture<?> fiveMinuteTask = taskScheduler.schedule(() -> {
            // 작업 스레드에 SecurityContext 설정
            SecurityContextHolder.setContext(context);
            try {
                isFiveMinutesPassed.set(true);
            } finally {
                // SecurityContext 정리
                SecurityContextHolder.clearContext();
            }
        }, new Date(System.currentTimeMillis() + Duration.ofSeconds(5).toMillis())); // 분으로 바꿔야댐
        scheduledTasks.put(user.getId(), fiveMinuteTask);
    }

    /**
     *
     * @param user  현재 유저
     * @param isFiveMinutesPassed   5분 지났는지 확인하는 플레그
     * @param isThirtyMinutesPassed    30분 지났는지 확인하는 플레그
     */
    @Override
    @Transactional
    public void handleRandomMatch(User user, AtomicBoolean isFiveMinutesPassed, AtomicBoolean isThirtyMinutesPassed) {
        double currentRange = rangeCalculator.calculateAssetRange(time.getAndIncrement());
            if (isThirtyMinutesPassed.get()) switchToQuickMatch(user);

            redisTemplate.execute((RedisCallback<Object>) connection -> {
                try {
                    connection.multi();  // 트랜잭션 시작

                    Long asset = balanceService.totalBalanceFindByUserId(user.getId()).get().getAsset();


                    // 5분 전 -> 10명, 5분 후 4명이상
                    int requiredUserCount = isFiveMinutesPassed.get() ? 4 : 10;


                    // 매칭 잡힌 유저들
                    List<User> potentialMatch = findPotentialMatches(asset, currentRange);
                    synchronized (this) {
                        if (potentialMatch.size() >= requiredUserCount) {
                            if (userRoomService.findCurrentRoom() == null) {
                                // 방에 들어가 있지 않은지 다시 확인
                                Long roomId = createRoomWithUsers(potentialMatch);
                                potentialMatch.forEach(potentialMatchUser -> {
                                    matchingQueueService.removeUserFromQueue(potentialMatchUser);
                                    notifyUser(potentialMatchUser, roomId);
                                });

                            }
                        } potentialMatch.forEach(user1 -> matchingQueueService.changeStatus(user1.getId()));
                    }
                    connection.exec();  // 트랜잭션 커밋
                } catch (Exception e) {
                    connection.discard();  // 롤백
                }
                return null;
            });

    }

    /**
     *
     * @param user
     */
    @Transactional
    public void handleQuickMatch(User user) {
        try {
            List<Room> availableRooms = findAvailableRooms();
            if (!availableRooms.isEmpty()) {
                // 가능한 방이 있다면, 첫 번째 방에 유저 입장
                Room room = availableRooms.get(0);
                roomService.enterRoom(user, room.getRoomId());
                notifyUser(user, room.getRoomId());
                cancelMatching(user);
                cancelCurrentUserScheduledTasks();
            } else { // 가능한 방이 없을 경우 방 생성

                Long latestRoomId = roomService.findRoomByLatestCreateDate().getRoomId();
                if (latestRoomId == null) {
                    latestRoomId = 0L;
                }
                // 초기 시드머니
                StartSeedMoney startSeedMoney = getStartSeedMoney(user);
                CreateRoomDTO createRoomDTO = CreateRoomDTO.builder()
                        .title(String.format("빠른 매칭 %d", latestRoomId))
                        .end(5) // 게임 라운드 수
                        .delay(1) // 시작 딜레이
                        .isDeleted(false)
                        .isStarted(false)
                        .createDate(LocalDateTime.now())
                        .startSeedMoney(startSeedMoney)
                        .build();


                Room room = roomService.createRoom(createRoomDTO, user);

                // 해당 유저에게 알림
                notifyUser(user, room.getRoomId());
                cancelMatching(user);
                cancelCurrentUserScheduledTasks();
            }
        } catch (Exception e) {
            cancelMatching(user);
            cancelCurrentUserScheduledTasks();
        }

    }

    /**
     *
     * @return
     */
    @Override
    @Transactional
    // 빠른 매칭 시 들어갈만한 방 찾아주기
    // 수정 필요
    // 자기가 들어갈 수 있는 방 중 가장 인원이 많은 방으로
    public List<Room> findAvailableRooms() {
        User user = findUserBySecurity.getCurrentUser();
        Long asset = balanceService.totalBalanceFindByUserId(user.getId()).get().getAsset();
        return roomService.findRoomsWithinAssetRange(asset / 3).stream()
                .sorted(Comparator.comparing(Room::getUserCount))
                .toList();
    }

    /**
     *
     * @param currentUserAsset     현재 유저 자산
     * @param range     현재 자산 조건 범위
     * @return 대기열에 있는 사람중 자산 조건에 맞는 사람 list
     */
    @Override
    @Transactional
    // 랜덤 매칭시 유저 찾아주는 메서드
    public List<User> findPotentialMatches(Long currentUserAsset, double range) {
        User currentUser = findUserBySecurity.getCurrentUser();
        Long asset = balanceService.totalBalanceFindByUserId(currentUser.getId()).get().getAsset();
        long min = (long) (asset - (asset * range / 100)); // 자산 범위 중 최소 값
        long max = (long) (asset + (asset * range / 100)); // 자산 범위 중 최대 값
        return matchingQueueService.getUsersInQueue(min,max);
    }

    /**
     * @param users 매칭이 잡힌 유저들
     */
    @Override
    @Transactional
    // 랜덤 매칭시 잡힌 유저들과 방 들어가기
    public Long createRoomWithUsers(List<User> users) {
        // 모든 유저 ID를 추출
        List<Long> userIds = users.stream()
                .map(User::getId)
                .toList();

        // 해당 유저들의 자산 정보만 조회
        List<TotalBalance> balances = userIds.stream()
                .map(userId -> balanceService.totalBalanceFindByUserId(userId).get())
                .toList();

        // 자산이 가장 많은 사용자 찾기
        User richestUser = balances.stream()
                .max(Comparator.comparing(TotalBalance::getAsset))
                .map(TotalBalance::getUser)
                .orElseThrow(() -> new ApiException(ExceptionEnum.USER_NOT_FOUND));

        // 자산이 가장 적은 사용자 찾기
        User poor = balances.stream()
                .min(Comparator.comparing(TotalBalance::getAsset))
                .map(TotalBalance::getUser)
                .orElseThrow(() -> new ApiException(ExceptionEnum.USER_NOT_FOUND));

        // 자산이 가장 적은 사용자 기준으로 시작 시드머니 설정
        StartSeedMoney startSeedMoney = getStartSeedMoney(poor);

        // 현재 생성되어 있는 방 중 가장 최신 방 roomId
        Long latestRoomId = roomService.findRoomByLatestCreateDate().getRoomId();
        if (latestRoomId == null) {
            latestRoomId = 0L;
        }
        CreateRoomDTO createRoomDTO = CreateRoomDTO.builder()
                .title(String.format("랜덤 매칭 %d", latestRoomId + 1))
                .end(5)
                .delay(1)
                .isDeleted(false)
                .isStarted(false)
                .createDate(LocalDateTime.now())
                .startSeedMoney(startSeedMoney) // 변경
                .build();

        Room room = roomService.createRoom(createRoomDTO, richestUser);
        // 각 사용자들 알람 및 방 입장
        users.forEach(user -> {
            roomService.enterRoom(user,room.getRoomId());
            cancelCurrentUserScheduledTasks();
            cancelMatching(user);
        });

        return room.getRoomId();
    }

    /**
     *
     * @param user  현재 유저
     */
    @Override
    @Transactional
    // 매칭 자동 변경
    public void switchToQuickMatch(User user) {
        cancelMatching(user);
        startQuickMatching();
    }

    /**
     *
     * @param user  현재 유저
     */
    @Override
    @Transactional
    public void cancelMatching(User user) {
        ScheduledFuture<?> matchingTask = matchingTasks.remove(user.getId());
        if (matchingTask != null) matchingTask.cancel(false);
    }

    @Override
    @Transactional
    public void cancelCurrentUserScheduledTasks() {
        User user = findUserBySecurity.getCurrentUser();
        // 매칭 큐에서 해당 유저 삭제
        matchingQueueService.removeUserFromQueue(user);

        // 모든 작업에 대해 종료 요청
        scheduledTasks.forEach((key, future) -> {
            if (!future.isDone()) {
                future.cancel(false);
            }
        });
        // 참조 제거 전 모든 상태 로깅
        scheduledTasks.clear();
    }

    // 시작 시드머니 설정
    public StartSeedMoney getStartSeedMoney(User user) {
        StartSeedMoney startSeedMoney = null;
        // 유저의 자산 1/3이상으로 시드머니 설정
        if (balanceService.totalBalanceFindByUserId(user.getId()).get().getAsset() / 3 >= 10000000) {
            startSeedMoney = StartSeedMoney.TEN_MILLION;
        }
        if (balanceService.totalBalanceFindByUserId(user.getId()).get().getAsset() / 3 >= 20000000) {
            startSeedMoney = StartSeedMoney.TWENTY_MILLION;
        }
        if (balanceService.totalBalanceFindByUserId(user.getId()).get().getAsset() / 3 >= 30000000) {
            startSeedMoney = StartSeedMoney.THIRTY_MILLION;
        }
        if (balanceService.totalBalanceFindByUserId(user.getId()).get().getAsset() / 3 >= 40000000) {
            startSeedMoney = StartSeedMoney.FORTY_MILLION;
        }

        return startSeedMoney;
    }

    // 각 사용자에게 방 정보를 전송하는 메서드
    @Transactional
    public void notifyUser(User user, Long roomId) {
        sseService.sendRoomNotification(user.getId(), roomId);
    }
}