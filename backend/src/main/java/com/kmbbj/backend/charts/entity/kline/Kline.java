package com.kmbbj.backend.charts.entity.kline;

import com.kmbbj.backend.charts.entity.coin.Coin;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "kline")
@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Kline {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long klineId;

    @Column(name = "interval")
    private String interval;  // 1m, 3m, 5m, 30m, 1d, 1w 등

    @Column(name = "open_price")
    private double openPrice;

    @Column(name = "close_price")
    private double closePrice;

    @Column(name = "high_price")
    private double highPrice;

    @Column(name = "low_price")
    private double lowPrice;

    @Column(name = "timezone")
    private Long timezone;

    @Column(name = "volume")
    private double volume;

    @Column(name = "ma10")
    private Double ma10;

    @Column(name = "ma20")
    private Double ma20;

    @Column(name = "ma30")
    private Double ma30;

    @Column(name = "bbu")
    private Double bbu;

    @Column(name = "bbd")
    private Double bbd;

    @ManyToOne
    @JoinColumn(name = "coin_id", nullable = false)
    private Coin coin;
}
