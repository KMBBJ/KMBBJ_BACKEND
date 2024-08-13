package com.kmbbj.backend.charts.service;

import com.kmbbj.backend.charts.dto.CoinResponse;
import com.kmbbj.backend.charts.entity.coin.Coin;
import com.kmbbj.backend.charts.entity.OrderType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CoinService {
    CoinResponse getCoinResponse(String symbol);
    Page<Coin> getAllCoins(Pageable pageable);
    void addCoin(String symbol, String coin_name);
    void deleteCoin(String symbol);
    void updateCoin(String symbol, String status, OrderType orderType);
}
