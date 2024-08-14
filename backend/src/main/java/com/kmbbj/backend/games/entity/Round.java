package com.kmbbj.backend.games.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 *  Round 엔티티
 *  게임 내의 한 라운드
 */
@Entity
@Table (name = "rounds")
@NoArgsConstructor
@Getter@Setter
public class Round {

    // 라운드 ID (기본 키)
    @Id @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column (name = "round_id", nullable = false)
    private Long roundId;

    /**
     *  라운드 번호
     *  게임 내에서 몇 번째 라운드인지 파악
     */
    @Column(name = "round_number")
    private Integer roundNumber;

    /**
     *  라운드의 지속 시간 (24시간)
     */
    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    /**
     * 라운드가 속한 게임
     * 다대일 관계 , 하나의 게임 여러 라운드 포함
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private Game game;

    /**
     *  라운드의 결과
     *  일대일 관계 , 각 라운드는 하나의 결과 가짐
     */
    @OneToOne(mappedBy = "round", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private RoundResult roundResult;

}
