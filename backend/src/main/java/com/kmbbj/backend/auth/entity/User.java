package com.kmbbj.backend.auth.entity;

import com.kmbbj.backend.global.entity.AuditingFields;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User extends AuditingFields {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, unique = true, name = "email")
    private String email;

    @Setter
    @Column(nullable = false, name = "nickname")
    private String nickname;

    @Setter
    @Column(name = "password")
    private String password;

    @Column(nullable = false, name = "authority")
    @Enumerated(EnumType.STRING)
    private Authority authority;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Setter
    @Column(name = "suspension_end_date")
    private LocalDateTime suspensionEndDate;

    // 정지 상태 확인 메서드
    public boolean isSuspended() {
        return suspensionEndDate != null && LocalDateTime.now().isBefore(suspensionEndDate);

    }
}