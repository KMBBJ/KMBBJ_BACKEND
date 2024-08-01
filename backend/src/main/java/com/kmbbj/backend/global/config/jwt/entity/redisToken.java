package com.kmbbj.backend.global.config.jwt.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RedisHash("Token")
public class redisToken implements Serializable {
    @Id
    private Long userId; // Redis의 Key로 사용자의 Id 사용
    @Setter
    private String refreshToken;
    private LocalDateTime timeOut;

    public redisToken refresh(String refreshToken, LocalDateTime timeOut) {
        return redisToken.builder()
                .userId(this.userId)
                .refreshToken(refreshToken)
                .timeOut(timeOut)
                .build();
    }
}
