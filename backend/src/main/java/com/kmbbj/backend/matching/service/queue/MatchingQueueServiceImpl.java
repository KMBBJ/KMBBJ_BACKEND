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
            User user = userService.UserfindById(Long.parseLong(userId)).orElseThrow(() -> new ApiException(ExceptionEnum.USER_NOT_FOUND));
            users.add(user);
        }
        return users;
    }

    @Override
    public void removeUserFromQueue(User user) {
        redisTemplate.opsForZSet().remove("matchingQueue", user.getId().toString());
    }

    @Override
    // 유저가 이미 매칭된 상태인지 확인하는 메서드 추가
    public boolean isUserAlreadyMatched(User user) {
        System.out.println(redisTemplate.opsForZSet().rank("matchingQueue",user.getId().toString()));
        return redisTemplate.opsForZSet().rank("matchingQueue", user.getId().toString()) == null;
    }
}