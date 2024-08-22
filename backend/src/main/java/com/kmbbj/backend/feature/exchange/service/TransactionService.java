package com.kmbbj.backend.feature.exchange.service;

import com.kmbbj.backend.feature.exchange.service.buy.cansel.CanselBuyOrder;
import com.kmbbj.backend.feature.exchange.service.buy.save.SaveBuyOrder;
import com.kmbbj.backend.feature.exchange.service.execution.matching.ExecutionAllMatchingOrder;
import com.kmbbj.backend.feature.exchange.service.sell.cansel.CanselSellOrder;
import com.kmbbj.backend.feature.exchange.service.sell.save.SaveSellOrder;
import org.springframework.stereotype.Service;

@Service
public interface TransactionService extends
        //Sell
        SaveSellOrder,
        CanselSellOrder,
        //Buy
        SaveBuyOrder,
        CanselBuyOrder,
        //excution
        ExecutionAllMatchingOrder
{}