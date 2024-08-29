package com.kmbbj.backend.feature.exchange.service.execution.matching;

public interface ExecutionAllMatchingOrder {
    void matchOrders(Long coinId, Long price);
}
