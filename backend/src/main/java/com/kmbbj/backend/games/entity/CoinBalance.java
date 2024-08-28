package com.kmbbj.backend.games.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "coin_balances")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoinBalance {
    @Id
    @Column(name = "coin_balances_id")
    private Long coinBalancesId;

    @Column(name = "game_balances_id")
    private Long gameBalanceId;

    @Column(name = "coin_id")
    private Long coinId;

    @Column(name = "quantity", precision = 20, scale = 10)
    private BigDecimal quantity;
}
