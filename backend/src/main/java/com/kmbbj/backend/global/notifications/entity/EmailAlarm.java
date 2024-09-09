package com.kmbbj.backend.global.notifications.entity;


import com.kmbbj.backend.feature.auth.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;


@Getter
@Setter
@Table(name = "email_alarms")
@Entity
public class EmailAlarm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long emailAlarmsId;

    @Column(nullable = false, length = 100)
    private String subject;

    @Column(nullable = false)
    private String message;

    private Timestamp createDateAlarms;

    @Enumerated(EnumType.STRING)
    @Column
    private TradeOrder tradeOrder;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Enum 정의
    public enum TradeOrder {
        BUY,
        SELL,
        START,
        END
    }
}

