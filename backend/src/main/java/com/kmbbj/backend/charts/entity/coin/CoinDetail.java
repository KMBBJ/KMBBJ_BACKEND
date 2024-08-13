package com.kmbbj.backend.charts.entity.coin;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "coin_details")
@Getter @Setter
@NoArgsConstructor
public class CoinDetail {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long coinDetailId;

    @Column(name = "price")
    private double price;

    @Column(name = "bid_price")
    private double bidPrice;

    @Column(name = "bid_qty")
    private double bidQty;

    @Column(name = "ask_price")
    private double askPrice;

    @Column(name = "ask_qty")
    private double askQty;

    @Column(name = "voting_amount")
    private double votingAmount;

    @Column(name = "timezone")
    private Timestamp timezone = Timestamp.valueOf(LocalDateTime.now());

    @ManyToOne
    @JoinColumn(name = "coin_id", nullable = false)
    private Coin coin;
}
