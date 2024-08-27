package com.kmbbj.backend.feature.exchange.service.execution.matching;

import java.math.BigDecimal;

public interface ExecutionAllMatchingOrder {
    void matchOrders(Long coinId, BigDecimal price);
}
