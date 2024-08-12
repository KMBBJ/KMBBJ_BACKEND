package com.kmbbj.backend.games.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table (name = "game_results")
@NoArgsConstructor
@Getter@Setter
public class GameResult {

    @Id @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column (name = "result_id")
    private Long resultId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private Game game;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "total_profit")
    private Integer totalProfit;

    @Column(name = "total_loss")
    private Integer totalLoss;

    @Column(name = "user_rank")
    private Integer userRank;

}
