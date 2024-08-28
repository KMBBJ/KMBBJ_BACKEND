package com.kmbbj.backend.charts.entity.coin;

import com.kmbbj.backend.charts.entity.CoinStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "coins")
@Getter
@Setter
@NoArgsConstructor
public class Coin {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long coinId;

    @Column(name = "coin_name")
    private String coinName; // 코인의 이름

    @Column(name = "symbol")
    private String symbol; // 코인의 심볼 (예: BTCUSDT, ETHUSDT)

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private CoinStatus status; // 코인의 현재 상태
}
