package com.kmbbj.backend.feature.charts.entity.kline;

import com.kmbbj.backend.feature.charts.entity.coin.Coin;
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
    private String interval;  // 캔들스틱의 시간 간격 (예: 1m, 3m, 5m, 30m, 1d, 1w 등)

    @Column(name = "open_price")
    private double openPrice; // 해당 간격에서의 시작 가격

    @Column(name = "close_price")
    private double closePrice; // 해당 간격에서의 종료 가격

    @Column(name = "high_price")
    private double highPrice; // 해당 간격에서의 최고 가격

    @Column(name = "low_price")
    private double lowPrice; // 해당 간격에서의 최저 가격

    @Column(name = "timezone")
    private Long timezone;  // 데이터의 시간대 정보 (UNIX 시간)

    @Column(name = "volume")
    private double volume;  // 해당 간격에서 거래된 총 수량

    @Column(name = "ma10")
    private Double ma10;  // 10일 이동 평균

    @Column(name = "ma20")
    private Double ma20;  // 20일 이동 평균

    @Column(name = "ma30")
    private Double ma30;  // 30일 이동 평균

    @Column(name = "bbu")
    private Double bbu;  // 볼린저 밴드 상단

    @Column(name = "bbd")
    private Double bbd;  // 볼린저 밴드 하단

    @ManyToOne
    @JoinColumn(name = "coin_id", nullable = false)
    private Coin coin;
}
