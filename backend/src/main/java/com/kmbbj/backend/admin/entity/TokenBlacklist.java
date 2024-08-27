package com.kmbbj.backend.admin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "token_blacklist")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenBlacklist {

    @Id
    @Column(name = "id") // 기본 키로 설정
    private Long id; // 유저 ID로 사용

    @Column(name = "token", nullable = false, unique = true)
    private String token;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;
}
