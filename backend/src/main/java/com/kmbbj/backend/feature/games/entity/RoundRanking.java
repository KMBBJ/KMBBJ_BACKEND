package com.kmbbj.backend.feature.games.entity;

import com.kmbbj.backend.feature.auth.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "round_rankings")
@NoArgsConstructor
@Getter@Setter
public class RoundRanking {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "round_ranking_id")
    private Long roundRankingId;

    @Column(name = "rank")
    private Integer rank;

    @Column(name = "profit")
    private String profit;

    @Column(name = "loss")
    private String loss;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id", nullable = false)
    private Round round;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
