package com.kmbbj.backend.feature.auth.entity;

import com.kmbbj.backend.global.entity.AuditingFields;
import jakarta.persistence.*;
import lombok.*;

@Entity
@ToString
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_sns")
public class UserSns  extends AuditingFields {
    @Id
    @Column(name = "user_sns_id")
    private Long userSnsId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "sns_id")
    private Long snsId;
}
