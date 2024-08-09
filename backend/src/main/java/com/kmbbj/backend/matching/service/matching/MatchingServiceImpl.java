package com.kmbbj.backend.matching.service.matching;

import com.kmbbj.backend.auth.entity.User;
import com.kmbbj.backend.auth.service.UserService;
import com.kmbbj.backend.balance.entity.TotalBalance;
import com.kmbbj.backend.balance.service.BalanceService;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import com.kmbbj.backend.global.config.security.FindUserBySecurity;
import com.kmbbj.backend.matching.dto.CreateRoomDTO;
import com.kmbbj.backend.matching.entity.Room;
import com.kmbbj.backend.matching.service.queue.MatchingQueueService;
import com.kmbbj.backend.matching.service.room.RoomService;
import lombok.RequiredArgsConstructor;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchingServiceImpl implements MatchingService {

    private final TaskScheduler taskScheduler;
    private final Map<Long, ScheduledFuture<?>> assetRangeTasks = new ConcurrentHashMap<>();
    private final Map<Long, ScheduledFuture<?>> matchingTasks = new ConcurrentHashMap<>();
    private final Map<Object, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    private final RedisTemplate<String, Object> redisTemplate;
    private final RoomService roomService;
    private final UserService userService;
    private final MatchingQueueService matchingQueueService;
    private final FindUserBySecurity findUserBySecurity;
    private final BalanceService balanceService;


    @Override
    @Transactional
    public void startQuickMatching() {
        User user = findUserBySecurity.getCurrentUser();
        matchingQueueService.addUserToQueue(user, true);
        scheduleMatchingTasks(user, 10, true);
    }


    @Override
    @Transactional
    public void startRandomMatching() {
        User user = findUserBySecurity.getCurrentUser();
        matchingQueueService.addUserToQueue(user, false);
        scheduleMatchingTasks(user, 1, false);
    }

    /**
     *
     * @param user  현재 유저
     * @param increment     자산 범위 증가량
     * @param isQuickMatch  빠른 매칭 여부
     */
    @Override
    @Transactional
    public void scheduleMatchingTasks(User user, int increment, boolean isQuickMatch) {
        AtomicBoolean isFiveMinutesPassed = new AtomicBoolean(false);
        AtomicBoolean isTenMinutesPassed = new AtomicBoolean(false);
        AtomicReference<Long> minUser = new AtomicReference<>(4L);
        System.out.println("scheduleMatchingTasks");
        // 현재 SecurityContext 를 저장
        SecurityContext context = SecurityContextHolder.getContext();

        // 자산 범위 설정
        ScheduledFuture<?> assetRangeTask = taskScheduler.scheduleAtFixedRate(() -> {
            // 작업 스레드에 SecurityContext 설정
            SecurityContextHolder.setContext(context);
            try {
                updateAssetRange(user, increment);
            } finally {
                // SecurityContext 정리
                SecurityContextHolder.clearContext();
            }
        }, Duration.ofMinutes(isQuickMatch ? 5 : 1));
        scheduledTasks.put(user.getId(), assetRangeTask);
        assetRangeTasks.put(user.getId(), assetRangeTask);

        // 매칭 실행 스케줄링
        ScheduledFuture<?> matchingTask = taskScheduler.scheduleWithFixedDelay(() -> {
            // 작업 스레드에 SecurityContext 설정
            SecurityContextHolder.setContext(context);
            try {
                if (isQuickMatch) {
                    handleQuickMatch(user,isQuickMatch,minUser);
                } else {
                    handleRandomMatch(user, isQuickMatch, isFiveMinutesPassed, isTenMinutesPassed);
                }

            } finally {
                // SecurityContext 정리
                SecurityContextHolder.clearContext();
            }
        }, 1000);
        scheduledTasks.put(user.getId(), matchingTask);
        matchingTasks.put(user.getId(), matchingTask);

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
        }, new Date(System.currentTimeMillis() + Duration.ofMinutes(1).toMillis()));
        scheduledTasks.put(user.getId(), fiveMinuteTask);

        // 시간 조건 설정 (10분)
        ScheduledFuture<?> tenMinuteTask = taskScheduler.schedule(() -> {
            // 작업 스레드에 SecurityContext 설정
            SecurityContextHolder.setContext(context);
            try {
                isTenMinutesPassed.set(true);
                if (!isQuickMatch) switchToQuickMatch(user);
            } finally {
                // SecurityContext 정리
                SecurityContextHolder.clearContext();
            }
        }, new Date(System.currentTimeMillis() + Duration.ofMinutes(1).toMillis()));
        scheduledTasks.put(user.getId(), tenMinuteTask);

        if (isQuickMatch) {
            // 최소 인원수 설정
            ScheduledFuture<?> minUserTask = taskScheduler.scheduleAtFixedRate(() -> {
                SecurityContextHolder.setContext(context);
                try {
                    minUser.updateAndGet(v -> Math.max(1, v - 1));

                } finally {
                    SecurityContextHolder.clearContext();
                }
            }, Duration.ofMinutes(1));
            scheduledTasks.put(user.getId(), minUserTask);
        }
    }

    /**
     *
     * @param user  현재 유저
     * @param isQuickMatch  빠른 매칭 여부
     * @param isFiveMinutesPassed   5분 지났는지 확인하는 플레그
     * @param isTenMinutesPassed    10분 지났는지 확인하는 플레그
     */
    @Override
    @Transactional
    // 랜덤 매칭시 방 만들어주는 로직
    public void handleRandomMatch(User user, boolean isQuickMatch, AtomicBoolean isFiveMinutesPassed, AtomicBoolean isTenMinutesPassed) {
        // 10분 후 루프 종료 조건 체크
        if (isTenMinutesPassed.get()) return;

        Long currentRange = (Long) redisTemplate.opsForHash().get("userAssetRanges", user.getId().toString());
        Long asset = balanceService.totalBalanceFindByUserId(user.getId()).orElseThrow(()->new ApiException(ExceptionEnum.BALANCE_NOT_FOUND)).getAsset();
        List<User> potentialMatches = findPotentialMatches(asset, currentRange);
        int requiredUserCount = isFiveMinutesPassed.get() ? 4 : 10;

        if (potentialMatches.size() >= requiredUserCount) {

            System.out.println("방 찾는중");
            System.out.println(currentRange);
            System.out.println(asset);
            createRoomWithUsers(potentialMatches);
            potentialMatches.forEach(completeUser -> matchingQueueService.removeUserFromQueue(completeUser, isQuickMatch));
        }
    }

    /**
     *
     * @param user  현재 유저
     * @param isQuickMatch  빠른 매칭 여부
     * @param minUser   최소 인원수
     */
    @Transactional
    public void handleQuickMatch(User user, boolean isQuickMatch, AtomicReference<Long> minUser) {
        Long range = (Long) redisTemplate.opsForHash().get("userAssetRanges", user.getId().toString());
        List<Room> availableRooms = findAvailableRooms(range, minUser);
        System.out.println(minUser);
        if (!availableRooms.isEmpty()) {
            // 가능한 방이 있다면, 첫 번째 방에 유저 입장
            Room room = availableRooms.get(0);
            roomService.enterRoom(room.getRoomId());
            System.out.println("User entered an existing room.");
            matchingQueueService.removeUserFromQueue(user,isQuickMatch);
            cancelCurrentUserScheduledTasks();
        }
        if (minUser.get() == 1){
            // 방 생성
            Long latestRoomId = roomService.findRoomByLatestCreateDate().getRoomId();
            CreateRoomDTO createRoomDTO = CreateRoomDTO.builder()
                    .title(String.format("빠른 매칭 %d", latestRoomId))
                    .end(5) // 게임 라운드 수
                    .delay(1) // 시작 딜레이
                    .isDeleted(false)
                    .isStarted(false)
                    .createDate(LocalDateTime.now())
                    .startSeedMoney(1000)
                    .build();
            roomService.createRoom(createRoomDTO, user);
            matchingQueueService.removeUserFromQueue(user,isQuickMatch);
            cancelCurrentUserScheduledTasks();
        }
    }

    /**
     *
     * @param range     현재 자산 조건 범위
     * @param minUser   현재 최소 유저수
     * @return 자산 범위에 맞고 최소인원수 이상인 방 list
     */
    @Override
    @Transactional
    // 빠른 매칭 시 들어갈만한 방 찾아주기
    public List<Room> findAvailableRooms(Long range, AtomicReference<Long> minUser) {
        System.out.println("findAvailableRooms");
        User user = findUserBySecurity.getCurrentUser();
        Long asset = balanceService.totalBalanceFindByUserId(user.getId()).orElseThrow(()->new ApiException(ExceptionEnum.BALANCE_NOT_FOUND)).getAsset();
        return roomService.findRoomsWithinAssetRange(asset, range).stream()
                .filter(room -> room.getUserCount() >= minUser.get())
                .sorted(Comparator.comparing(Room::getUserCount))
                .collect(Collectors.toList());
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
    public List<User> findPotentialMatches(Long currentUserAsset, Long range) {
        System.out.println("findPotentialMatches");
        User currentUser = findUserBySecurity.getCurrentUser();
        Long asset = balanceService.totalBalanceFindByUserId(currentUser.getId()).orElseThrow(()->new ApiException(ExceptionEnum.BALANCE_NOT_FOUND)).getAsset();
        return matchingQueueService.getUsersInQueue(false).stream()
                .filter(user -> asset >= (currentUserAsset - (currentUserAsset * range / 100)) && asset <= (currentUserAsset + (currentUserAsset * range / 100)))
                .collect(Collectors.toList());
    }

    /**
     *
     * @param users     매칭이 잡힌 유저들
     */
    @Override
    @Transactional
    // 랜덤 매칭시 잡힌 유저들과 방 들어가기
    public void createRoomWithUsers(List<User> users) {
        System.out.println("createRoomWithUsers");
        // 모든 유저 ID를 추출
        List<Long> userIds = users.stream()
                .map(User::getId)
                .collect(Collectors.toList());

        // 해당 유저들의 자산 정보만 조회
        List<TotalBalance> balances = userIds.stream()
                .map(userId -> balanceService.totalBalanceFindByUserId(userId).orElseThrow(()->new ApiException(ExceptionEnum.BALANCE_NOT_FOUND)))
                .toList();

        // 자산이 가장 많은 사용자 찾기
        User richestUser = balances.stream()
                .max(Comparator.comparing(TotalBalance::getAsset))
                .map(TotalBalance::getUser)
                .orElseThrow(() -> new ApiException(ExceptionEnum.USER_NOT_FOUND));
        Long latestRoomId = roomService.findRoomByLatestCreateDate().getRoomId();
        CreateRoomDTO createRoomDTO = CreateRoomDTO.builder()
                .title(String.format("랜덤 매칭 %d", latestRoomId + 1))
                .end(5)
                .delay(1)
                .isDeleted(false)
                .isStarted(false)
                .createDate(LocalDateTime.now())
                .startSeedMoney(1000)
                .build();

        System.out.println("방 생성 .........");
        cancelCurrentUserScheduledTasks();
        roomService.createRoom(createRoomDTO, richestUser);

        users.forEach(user -> roomService.enterRoom( latestRoomId + 1));
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
//        matchingQueueService.addUserToQueue(user,true);
        startQuickMatching();
        System.out.println("빠른 매칭으로 변경");
    }

    /**
     *
     * @param user        현재 유저
     * @param increment   자산 증가율
     */
    @Override
    @Transactional
    // 자산 범위 업데이트
    public void updateAssetRange(User user, int increment) {
        System.out.println("updateAssetRange");

        Long asset = balanceService.totalBalanceFindByUserId(user.getId()).orElseThrow(()->new ApiException(ExceptionEnum.BALANCE_NOT_FOUND)).getAsset();
        // Redis 에서 사용자의 현재 자산 범위를 가져옴
        Long currentRange = (Long) redisTemplate.opsForHash().get("userAssetRanges", user.getId().toString());
        if (currentRange == null) {
            currentRange = asset * 10 / 100; // 초기 10% 설정
        } else {
            currentRange += asset * increment / 100;
        }
        // 업데이트된 범위를 Redis 에 저장
        redisTemplate.opsForHash().put("userAssetRanges", user.getId().toString(), currentRange);
    }

    /**
     *
     * @param user  현재 유저
     */
    @Override
    @Transactional
    public void cancelMatching(User user) {
        System.out.println("cancelMatching");
        ScheduledFuture<?> assetRangeTask = assetRangeTasks.remove(user.getId());
        ScheduledFuture<?> matchingTask = matchingTasks.remove(user.getId());
        if (assetRangeTask != null) assetRangeTask.cancel(false);
        if (matchingTask != null) matchingTask.cancel(false);
    }

    @Transactional
    // 모든 유저의 작업 취소
    // 관리자 전용
    public void cancelAllUserScheduledTasks() {
        for (Long userId : assetRangeTasks.keySet()) {
            User user = userService.UserfindById(userId).orElseThrow(() -> new ApiException(ExceptionEnum.USER_NOT_FOUND));
            cancelMatching(user);
        }
    }

    @Override
    @Transactional
    public void cancelCurrentUserScheduledTasks() {
        scheduledTasks.forEach((key, future) -> {
            if (!future.isCancelled()) {
                // 모든 작업 강제 종료
                future.cancel(true);
            }
        });
        // 모든 참조 제거
        scheduledTasks.clear();
        System.out.println("cancelCurrentUserScheduledTasks");
    }
}