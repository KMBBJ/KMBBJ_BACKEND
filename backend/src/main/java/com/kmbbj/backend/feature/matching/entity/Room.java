package com.kmbbj.backend.feature.matching.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "rooms")
@Getter
@Setter
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "start_seed_money", nullable = false)
    @Enumerated(EnumType.STRING)
    private StartSeedMoney startSeedMoney;

    @Column(name = "end_round", nullable = false)
    private Integer end;

    @Column(name = "create_date", nullable = false)
    private LocalDateTime createDate = LocalDateTime.now();

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    @Column(name = "is_started", nullable = false)
    private Boolean isStarted;

    @Column(name = "delay")
    private Integer delay;

    @Column(name = "user_count")
    private Integer userCount;

    @Column(name = "average_asset", nullable = false)
    private Long averageAsset;

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY)
    private List<UserRoom> userRooms;

    public Long startSeedMoneyLong() {
        return (long) startSeedMoney.getAmount();
    }
}