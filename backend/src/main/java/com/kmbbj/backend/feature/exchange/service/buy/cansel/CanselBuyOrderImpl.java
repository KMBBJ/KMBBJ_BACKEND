package com.kmbbj.backend.feature.exchange.service.buy.cansel;

import com.kmbbj.backend.feature.exchange.entity.cassandra.BuyOrder;
import com.kmbbj.backend.feature.exchange.repository.cassandra.buy.BuyOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CanselBuyOrderImpl implements CanselBuyOrder {
    private final BuyOrderRepository buyOrderRepository;

}