package com.kmbbj.backend.games.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *  RoundResult 엔티티
 *  각 라운드의 결과 저장
 */
@Entity
@Table (name = "round_results")
@NoArgsConstructor
@Getter@Setter
public class RoundResult {

    // 라운드 결과 ID  (기본 키)
    @Id @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column (name = "round_result_id")
    private Long roundResultId;

    /**
     *  결과가 속한 라운드
     *  일대일 관계, 각 결과는 하나의 라운드에 속함
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id", nullable = false)
    private Round round;

    /**
     *  해당 라운드에서 가장 많이 구매된 코인
     */
    @Column(name = "top_buy_coin")
    private String topBuyCoin;

    /**
     *  가장 많이 구매된 코인의 구매 비율 (%)
     */
    @Column(name = "top_buy_percent")
    private String topBuyPercent;

    /**
     *  해당 라운드에서 가장 높은 수익을 낸 코인
     */
    @Column(name = "top_profit_coin")
    private String topProfitCoin;

    /**
     * 가장 높은 수익을 낸 코인의 수익률 (%)
     */
    @Column(name = "top_profit_percent")
    private String topProfitPercent;

    /**
     * 해당 라운드에서 가장 큰 손실을 본 코인
     */
    @Column(name = "top_loss_coin")
    private String topLossCoin;

    /**
     * * 가장 큰 손실을 본 코인의 손실률 (&)
     */
    @Column(name = "top_loss_percent")
    private String topLossPercent;

}
