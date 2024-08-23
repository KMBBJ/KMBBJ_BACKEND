package com.kmbbj.backend.feature.exchange.service;

import com.kmbbj.backend.feature.exchange.controller.request.OrderRequest;
import com.kmbbj.backend.feature.exchange.service.buy.save.SaveBuyOrder;
import com.kmbbj.backend.feature.exchange.service.sell.save.SaveSellOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    @Qualifier("saveSellOrderImpl")
    private final SaveSellOrder saveSellOrder;
    @Qualifier("saveBuyOrderImpl")
    private final SaveBuyOrder saveBuyOrder;

    @Override
    public void saveBuyOrder(OrderRequest orderRequest) {
        saveBuyOrder.saveBuyOrder(orderRequest);
    }

    @Override
    public void saveSellOrder(OrderRequest orderRequest) {
        saveSellOrder.saveSellOrder(orderRequest);
    }
}
