package com.kmbbj.backend.feature.matching.service.queue;

import com.kmbbj.backend.feature.auth.entity.User;

import java.util.List;

public interface MatchingQueueService {
    void addUserToQueue(User user);

    List<User> getUsersInQueue(double min, double max);

    void removeUserFromQueue(User user);

    boolean isUserAlreadyMatched(User user);

    void changeStatus(Long userId);
}
