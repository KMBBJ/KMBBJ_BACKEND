package com.kmbbj.backend.matching.service.queue;

import com.kmbbj.backend.auth.entity.User;
import com.kmbbj.backend.auth.service.UserService;
import com.kmbbj.backend.balance.repository.totalbalances.TotalBalancesRepository;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchingQueueServiceImpl implements MatchingQueueService {

    private final RedisTemplate<String, String> redisTemplate;

    private final UserService userService;

    private final TotalBalancesRepository totalBalancesRepository;

    @Override
    public void addUserToQueue(User user, boolean isQuickMatch) {
        Long asset = totalBalancesRepository.findByUserId(user.getId()).get().getAsset();
        String key = isQuickMatch ? "quickMatchQueue" : "randomMatchQueue";
        String value = String.format("%d:%d", user.getId(), asset);
        redisTemplate.opsForList().rightPush(key, value);
    }

    @Override
    public List<User> getUsersInQueue(boolean isQuickMatch) {
        String key = isQuickMatch ? "quickMatchQueue" : "randomMatchQueue";
        List<String> entries = redisTemplate.opsForList().range(key, 0, -1);
        List<User> users = new ArrayList<>();
        for (String entry : entries) {
            String[] parts = entry.split(":");
            Long userId = Long.parseLong(parts[0]);
            Long asset = Long.parseLong(parts[1]);
            User user = userService.UserfindById(userId).orElseThrow(() -> new ApiException(ExceptionEnum.USER_NOT_FOUND));
            users.add(user);
        }
        return users;
    }

    @Override
    public void removeUserFromQueue(User user, boolean isQuickMatch) {
        Long asset = totalBalancesRepository.findByUserId(user.getId()).get().getAsset();
        String key = isQuickMatch ? "quickMatchQueue" : "randomMatchQueue";
        String value = String.format("%d:%d", user.getId(), asset);
        redisTemplate.opsForList().remove(key, 1, value);
    }
}
