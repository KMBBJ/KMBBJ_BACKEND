package com.kmbbj.backend.charts.entity.coin;

import com.kmbbj.backend.charts.entity.OrderType;
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
    private String status; // 코인의 현재 상태

    @Column(name = "order_types")
    private OrderType orderTypes; // 코인에 설정 가능한 주문 유형 (STOP_LOSS, TAKE_PROFIT).
                                  // 각 유형에 대한 자세한 설명은 OrderType enum 참조.
}
