package com.kmbbj.backend.charts.entity.coin;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "coin24h_details")
@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Coin24hDetail {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coin_detail_id")
    @Id
    private Long coinDetailId;

    @Column(name = "price")
    private double price;  // 최종 거래 가격 (lastPrice)

    @Column(name = "bid_price")
    private double bidPrice;  // 현재 매수 호가 (bidPrice)

    @Column(name = "bid_qty")
    private double bidQty;  // 현재 매수 잔량 (bidQty)

    @Column(name = "ask_price")
    private double askPrice;  // 현재 매도 호가 (askPrice)

    @Column(name = "ask_qty")
    private double askQty;  // 현재 매도 잔량 (askQty)

    @Column(name = "price_change")
    private double priceChange;  // 가격 변동 (priceChange)

    @Column(name = "price_change_percent")
    private double priceChangePercent;  // 가격 변동률

    @Column(name = "weighted_avg_price")
    private double weightedAvgPrice;  // 가중 평균 가격

    @Column(name = "prev_close_price")
    private double prevClosePrice;  // 이전 마감 가격

    @Column(name = "open_price")
    private double openPrice;  // 개장 가격

    @Column(name = "high_price")
    private double highPrice;  // 최고가

    @Column(name = "low_price")
    private double lowPrice;  // 최저가

    @Column(name = "volume")
    private double volume;  // 거래량

    @Column(name = "quote_volume")
    private double quoteVolume;  // 견적 거래량

    @Column(name = "trade_count")
    private Long tradeCount;  // 거래 횟수

    @Column(name = "open_time")
    private Long openTime;  // 개장 시간

    @Column(name = "close_time")
    private Long closeTime;  // 마감 시간

    @Column(name = "timezone")
    private LocalDateTime timezone; // 데이터가 저장된 시간대 (기존 필드)

    @ManyToOne
    @JoinColumn(name = "coin_id")
    private Coin coin;  // 연관된 코인 엔티티 (기존 필드)
}