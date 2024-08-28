package com.kmbbj.backend.feature.exchange.service.sell.availablecoinds;

import com.kmbbj.backend.feature.exchange.controller.request.AvailableSellCoinsRequest;
import com.kmbbj.backend.feature.exchange.controller.response.AvailableSellCoinsResponse;

public interface FindAvailableCoins {
    AvailableSellCoinsResponse findAvailableCoins(AvailableSellCoinsRequest request);
}
