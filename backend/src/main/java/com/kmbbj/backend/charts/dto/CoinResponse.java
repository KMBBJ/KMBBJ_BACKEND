package com.kmbbj.backend.charts.dto;

import com.kmbbj.backend.charts.entity.coin.Coin;
import com.kmbbj.backend.charts.entity.coin.Coin24hDetail;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter @Setter
public class CoinResponse {
    private Coin coin;
    private Coin24hDetail coin24hDetail;
}
