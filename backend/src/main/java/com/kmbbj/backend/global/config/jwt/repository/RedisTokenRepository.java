package com.kmbbj.backend.global.config.jwt.repository;

import com.kmbbj.backend.global.config.jwt.entity.redisToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * redis를 사용하는 토큰 리파지토리
 */
@Repository
public interface RedisTokenRepository extends CrudRepository<redisToken, String> {
    Optional<redisToken> findByUserId(Long userId);
    Optional<redisToken> findByRefreshToken(String refreshToken);
}