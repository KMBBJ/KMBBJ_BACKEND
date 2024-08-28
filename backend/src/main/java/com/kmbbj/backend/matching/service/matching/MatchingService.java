package com.kmbbj.backend.matching.service.matching;

import com.kmbbj.backend.auth.entity.User;
import com.kmbbj.backend.matching.entity.Room;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public interface MatchingService {
    void startQuickMatching();

    void startRandomMatching();

    void cancelMatching(User user);

    List<User> findPotentialMatches(Long currentUserAsset, double range);

    void createRoomWithUsers(List<User> users);

    List<Room> findAvailableRooms();

    void scheduleMatchingTasks(User user, boolean isQuickMatch);

    void handleRandomMatch(User user, AtomicBoolean isFiveMinutesPassed, AtomicBoolean isThirtyMinutesPassed);

    void switchToQuickMatch(User user);

    void cancelCurrentUserScheduledTasks();


}
