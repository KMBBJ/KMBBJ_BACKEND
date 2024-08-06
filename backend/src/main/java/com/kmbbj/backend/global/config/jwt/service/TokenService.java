package com.kmbbj.backend.global.config.jwt.service;

import com.kmbbj.backend.global.config.jwt.entity.redisToken;
import com.kmbbj.backend.global.config.jwt.repository.RedisTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final RedisTokenRepository tokenRepository;

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
            oldToken.get().refresh(token.getRefreshToken(), calculateTimeout());
            tokenRepository.save(oldToken.get());
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
     * @param refreshToken 무효화할 리프레시 토큰
     */
    @Transactional
    public void invalidateRefreshToken(String refreshToken) {
        Optional<redisToken> tokenOptional = tokenRepository.findByRefreshToken(refreshToken);
        tokenOptional.ifPresent(tokenRepository::delete);
    }
}
