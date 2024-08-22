package com.kmbbj.backend.feature.exchange.service;

import com.kmbbj.backend.feature.exchange.service.buy.Buy;
import com.kmbbj.backend.feature.exchange.service.execution.Execution;
import com.kmbbj.backend.feature.exchange.service.sell.Sell;

public interface TransactionService extends
        Buy,
        Sell,
        Execution
{}