package com.kmbbj.backend.games.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table (name = "round_results")
@NoArgsConstructor
@Getter@Setter
public class RoundResult {

    @Id @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column (name = "round_result_id")
    private Long roundResultId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id", nullable = false)
    private Round round;
    @Column(name = "top_buy_coin")
    private String topBuyCoin;

    @Column(name = "top_buy_percent")
    private String topBuyPercent;

    @Column(name = "top_profit_coin")
    private String topProfitCoin;

    @Column(name = "top_profit_percent")
    private String topProfitPercent;

    @Column(name = "top_loss_coin")
    private String topLossCoin;

    @Column(name = "top_loss_percent")
    private String topLossPercent;

}
