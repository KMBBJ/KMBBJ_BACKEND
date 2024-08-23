package com.kmbbj.backend.feature.exchange.service.buy.save;

import com.kmbbj.backend.feature.exchange.controller.request.OrderRequest;

public interface SaveBuyOrder {
    void saveBuyOrder(OrderRequest orderRequest);
}