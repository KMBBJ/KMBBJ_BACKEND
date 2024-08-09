package com.kmbbj.backend.matching.service.matching;

import com.kmbbj.backend.auth.entity.User;
import com.kmbbj.backend.matching.entity.Room;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public interface MatchingService {
    void startQuickMatching();

    void startRandomMatching();

    void updateAssetRange(User user, int increment);

    void cancelMatching(User user);

    List<User> findPotentialMatches(Long asset, Long range);

    void createRoomWithUsers(List<User> users);

    List<Room> findAvailableRooms(Long range, AtomicReference<Long> minUser);

    void scheduleMatchingTasks(User user, int increment, boolean isQuickMatch);

    void handleRandomMatch(User user, boolean isQuickMatch, AtomicBoolean isFiveMinutesPassed, AtomicBoolean isTenMinutesPassed);

    void switchToQuickMatch(User user);

    void cancelCurrentUserScheduledTasks();


}
