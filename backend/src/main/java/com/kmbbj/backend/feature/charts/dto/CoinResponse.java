package com.kmbbj.backend.feature.charts.dto;

import com.kmbbj.backend.feature.charts.entity.coin.Coin;
import com.kmbbj.backend.feature.charts.entity.coin.Coin24hDetail;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter @Setter
public class CoinResponse {
    private Coin coin;
    private Coin24hDetail coin24hDetail;
}
