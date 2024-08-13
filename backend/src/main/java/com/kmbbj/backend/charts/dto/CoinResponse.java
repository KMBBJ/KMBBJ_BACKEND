package com.kmbbj.backend.charts.dto;

import com.kmbbj.backend.charts.entity.coin.Coin;
import com.kmbbj.backend.charts.entity.coin.CoinDetail;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter @Setter
public class CoinResponse {
    private Coin coin;
    private CoinDetail coinDetail;
}
