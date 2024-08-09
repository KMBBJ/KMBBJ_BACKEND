package com.kmbbj.backend.matching.service.queue;

import com.kmbbj.backend.auth.entity.User;

import java.util.List;

public interface MatchingQueueService {
    void addUserToQueue(User user, boolean isQuickMatch);

    List<User> getUsersInQueue(boolean isQuickMatch);

    void removeUserFromQueue(User user, boolean isQuickMatch);
}
