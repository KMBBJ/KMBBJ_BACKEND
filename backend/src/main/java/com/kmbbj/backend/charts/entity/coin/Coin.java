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
    private String coinName;

    @Column(name = "symbol")
    private String symbol;

    @Column(name = "status")
    private String status;

    @Column(name = "order_types")
    private OrderType orderTypes;
}
