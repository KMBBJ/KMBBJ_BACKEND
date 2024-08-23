package com.kmbbj.backend.feature.exchange.service.sell.save;

import com.kmbbj.backend.feature.exchange.controller.request.OrderRequest;

public interface SaveSellOrder {
    void saveSellOrder(OrderRequest orderRequest);
}
