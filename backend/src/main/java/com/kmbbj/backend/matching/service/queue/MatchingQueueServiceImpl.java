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
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MatchingQueueServiceImpl implements MatchingQueueService {

    private final RedisTemplate<String, String> redisTemplate;

    private final UserService userService;

    private final TotalBalancesRepository totalBalancesRepository;

    @Override
    public void addUserToQueue(User user) {
        Long asset = totalBalancesRepository.findByUserId(user.getId()).get().getAsset();
        redisTemplate.opsForZSet().add("matchingQueue", user.getId().toString(),asset);
    }

    @Override
    public List<User> getUsersInQueue(double min, double max) {
        Set<String> userIds = redisTemplate.opsForZSet().rangeByScore("matchingQueue", min, max);
        List<User> users = new ArrayList<>();
        for (String userId : userIds) {
            String status = redisTemplate.opsForValue().get("userStatus:" + userId);
            if (status == null && users.size() < 10) {  // 상태가 설정되지 않았다면 대기 중
                User user = userService.UserfindById(Long.parseLong(userId)).orElseThrow(() -> new ApiException(ExceptionEnum.USER_NOT_FOUND));
                users.add(user);
                redisTemplate.opsForValue().set("userStatus:" + userId, "selected");
            }
        }
        return users;
    }

    @Override
    public void changeStatus(Long userId) {
        redisTemplate.delete("userStatus:" + userId.toString());
    }

    @Override
    public void removeUserFromQueue(User user) {
        redisTemplate.opsForZSet().remove("matchingQueue", user.getId().toString());
        redisTemplate.delete("userStatus:" + user.getId().toString());
    }

    @Override
    public boolean isUserAlreadyMatched(User user) {
        String status = redisTemplate.opsForValue().get("userStatus:" + user.getId().toString());
        return "selected".equals(status);
    }
}