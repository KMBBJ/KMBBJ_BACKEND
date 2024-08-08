package com.kmbbj.backend.global.config.jwt.service;

import com.kmbbj.backend.global.config.jwt.entity.redisToken;
import com.kmbbj.backend.global.config.jwt.repository.RedisTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final RedisTokenRepository tokenRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${JWT_REFRESHTOKENEXPIRE}")
    private long refreshTokenExpire;

    /**
     * 토큰 사용자의 토큰이 저장되어 있을 경우 update, 없을 경우 create
     * @param token Token 데이터
     */
    @Transactional
    public void saveOrRefresh(redisToken token) {
        Optional<redisToken> oldToken = tokenRepository.findByUserId(token.getUserId());
        if (oldToken.isPresent()) {
            tokenRepository.save(oldToken.get().refresh(token.getRefreshToken(), calculateTimeout()));
        } else {
            tokenRepository.save(token);
        }
    }

    public LocalDateTime calculateTimeout() {
        Duration duration = Duration.ofMillis(refreshTokenExpire);
        return LocalDateTime.now().plus(duration);
    }

    /**
     * 리프레시 토큰을 무효화합니다.
     *
     * @param userId 사용자 Id
     */
    @Transactional
    public void invalidateRefreshToken(Long userId) {
        Optional<redisToken> tokenOptional = tokenRepository.findByUserId(userId);
        tokenOptional.ifPresent(token -> {
            tokenRepository.delete(token);
            // Redis에서 키 삭제
            redisTemplate.delete("userId:" + token.getUserId());
        });
    }
}
