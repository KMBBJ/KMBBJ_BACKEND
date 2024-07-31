package com.kmbbj.backend.matching.entity;

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
    private Integer startSeedMoney;

    @Column(name = "end", nullable = false)
    private Integer end;

    @Column(name = "create_date", nullable = false)
    private LocalDateTime createDate = LocalDateTime.now();

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    @Column(name = "is_started", nullable = false)
    private Boolean isStarted;

    @Column(name = "delay")
    private Integer delay;

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY)
    private List<UserRoom> userRooms;
}
