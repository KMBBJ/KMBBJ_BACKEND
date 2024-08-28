package com.kmbbj.backend.matching.service.matching;

import com.kmbbj.backend.auth.entity.User;
import com.kmbbj.backend.auth.service.UserService;
import com.kmbbj.backend.balance.entity.TotalBalance;
import com.kmbbj.backend.balance.service.BalanceService;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import com.kmbbj.backend.global.config.security.FindUserBySecurity;
import com.kmbbj.backend.global.config.websocket.MatchWebSocketHandler;
import com.kmbbj.backend.matching.dto.CreateRoomDTO;
import com.kmbbj.backend.matching.entity.Room;
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
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class MatchingServiceImpl implements MatchingService{

    private final MatchWebSocketHandler matchWebSocketHandler;
    private final TaskScheduler taskScheduler;
    private final Map<Long, ScheduledFuture<?>> matchingTasks = new ConcurrentHashMap<>();
    private final Map<Object, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    private final RedisTemplate<String, Object> redisTemplate;
    private final RoomService roomService;
    private final UserRoomService userRoomService;
    private final MatchingQueueService matchingQueueService;
    private final FindUserBySecurity findUserBySecurity;
    private final BalanceService balanceService;
    volatile boolean isShutdownRequested = false;
    private final AssetRangeCalculator rangeCalculator = new AssetRangeCalculator();
    private final AtomicInteger time = new AtomicInteger(0);

    @Override
    @Transactional
    public void startQuickMatching() {
        User user = findUserBySecurity.getCurrentUser();
        isShutdownRequested = false;
        matchingQueueService.addUserToQueue(user);
        scheduleMatchingTasks(user,true);
    }


    @Override
    @Transactional
    public void startRandomMatching() {
        if (userRoomService.findCurrentRoom() != null) {
            throw new ApiException(ExceptionEnum.IN_OTHER_ROOM);
        }

        User user = findUserBySecurity.getCurrentUser();
        TotalBalance totalBalance = balanceService.totalBalanceFindByUserId(user.getId()).orElse(null);
        if (totalBalance == null) {
            cancelCurrentUserScheduledTasks();
            throw new ApiException(ExceptionEnum.BALANCE_NOT_FOUND);
        }
        isShutdownRequested = false;
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

        // 매칭 실행 스케줄링
        ScheduledFuture<?> matchingTask = taskScheduler.scheduleWithFixedDelay(() -> {

            // 작업 스레드에 SecurityContext 설정
            SecurityContextHolder.setContext(context);
            try {
                if (isShutdownRequested || Thread.interrupted()) return;
                if (isQuickMatch) {
                    handleQuickMatch(user);
                    System.out.println("matchingTask1");
                } else {
                    handleRandomMatch(user, isFiveMinutesPassed, isThirtyMinutesPassed);
                    System.out.println("matchingTask2");
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
                if (isShutdownRequested || Thread.interrupted()) return;
                isThirtyMinutesPassed.set(true);
                System.out.println("isThirtyMinutesPassed");
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
                if (isShutdownRequested || Thread.interrupted()) return;
                isFiveMinutesPassed.set(true);
                System.out.println("isFiveMinutesPassed");
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
        if (matchingQueueService.isUserAlreadyMatched(user)) {
            return;  // 유저가 이미 매칭된 상태라면 반환
        }

        double currentRange = rangeCalculator.calculateAssetRange(time.getAndIncrement());
        synchronized (this) {  // 동기화 블록 추가
            if (isThirtyMinutesPassed.get()) switchToQuickMatch(user);

            redisTemplate.execute((RedisCallback<Object>) connection -> {
                connection.multi();  // 트랜잭션 시작

                Long asset = balanceService.totalBalanceFindByUserId(user.getId()).get().getAsset();
                List<User> potentialMatch = findPotentialMatches(asset, currentRange);

                int requiredUserCount = isFiveMinutesPassed.get() ? 4 : 10;

                if (potentialMatch.size() >= requiredUserCount) {
                    createRoomWithUsers(potentialMatch);
                    potentialMatch.forEach(matchingQueueService::removeUserFromQueue);
                }

                connection.exec();  // 트랜잭션 커밋
                return null;
            });
        }
    }

    /**
     *
     * @param user
     */
    @Transactional
    public void handleQuickMatch(User user) {
        List<Room> availableRooms = findAvailableRooms();
        if (!availableRooms.isEmpty()) {
            // 가능한 방이 있다면, 첫 번째 방에 유저 입장
            Room room = availableRooms.get(0);
            roomService.enterRoom(room.getRoomId());
            matchWebSocketHandler.notifyAboutMatch(user.getId(),room.getRoomId());
            cancelMatching(user);
            cancelCurrentUserScheduledTasks();
        }
        else {
            // 방 생성
            Long latestRoomId = roomService.findRoomByLatestCreateDate().getRoomId();
            // 초기 시드머니
            long startSeedMoney = balanceService.totalBalanceFindByUserId(user.getId()).get().getAsset() / 3;
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
            matchWebSocketHandler.notifyAboutMatch(user.getId(),room.getRoomId());
            cancelMatching(user);
            cancelCurrentUserScheduledTasks();
            System.out.println("handleQuickMatch");
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
     *
     * @param users     매칭이 잡힌 유저들
     */
    @Override
    @Transactional
    // 랜덤 매칭시 잡힌 유저들과 방 들어가기
    public void createRoomWithUsers(List<User> users) {
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

        User poor = balances.stream()
                .min(Comparator.comparing(TotalBalance::getAsset))
                .map(TotalBalance::getUser)
                .orElseThrow(() -> new ApiException(ExceptionEnum.USER_NOT_FOUND));
        long startSeedMoney = balanceService.totalBalanceFindByUserId(poor.getId()).get().getAsset() / 3;
        Long latestRoomId = roomService.findRoomByLatestCreateDate().getRoomId();
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
        matchWebSocketHandler.notifyAboutMatch(richestUser.getId(),room.getRoomId());
        users.forEach(user -> {
            matchWebSocketHandler.notifyAboutMatch(user.getId(), room.getRoomId());
            cancelCurrentUserScheduledTasks();
            cancelMatching(user);
        });
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
        System.out.println("switchToQuickMatch");
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
        System.out.println("cancelMatching");
    }

    @Override
    @Transactional
    public void cancelCurrentUserScheduledTasks() {
        User user = findUserBySecurity.getCurrentUser();
        // 모든 작업에 대해 종료 요청
        matchingQueueService.removeUserFromQueue(user);
        isShutdownRequested = true;
        scheduledTasks.forEach((key, future) -> {
            if (!future.isDone()) {
                boolean wasCancelled = future.cancel(false);
                if (wasCancelled) {
                    System.out.println("종료 성공" + key);
                } else {
                    System.out.println("종료 실패 " + key);
                }
            }
        });
        // 참조 제거 전 모든 상태 로깅
        scheduledTasks.clear();
    }
}