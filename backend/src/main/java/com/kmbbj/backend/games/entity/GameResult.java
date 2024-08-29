package com.kmbbj.backend.games.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * GameResult 엔티티
 * 게임의 최종 결과 저장
 */
@Entity
@Table (name = "game_results")
@NoArgsConstructor
@Getter@Setter
public class GameResult {

    // 게임 결과 ID (기본 키)
    @Id @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column (name = "result_id")
    private Long resultId;

    /**
     * 결과가 속한 게임
     * 일대일 관계 , 각 결과는 하나의 게임 속함
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private Game game;

    /**
     *  결과를 기록하는 사용자 ID
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     * 사용자의 총 수익
     */
    @Column(name = "total_profit")
    private Long totalProfit;

    /**
     *  사용자의 총 손실
     */
    @Column(name = "total_loss")
    private Long totalLoss;

    /**
     * 게임에서의 사용자 최종 순위
     */
    @Column(name = "user_rank")
    private Integer userRank;

}
