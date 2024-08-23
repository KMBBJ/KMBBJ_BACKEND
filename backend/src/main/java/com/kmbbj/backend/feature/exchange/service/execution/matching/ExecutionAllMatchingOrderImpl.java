package com.kmbbj.backend.feature.exchange.service.execution.matching;

import com.kmbbj.backend.feature.exchange.entity.cassandra.BuyOrder;
import com.kmbbj.backend.feature.exchange.entity.cassandra.SellOrder;
import com.kmbbj.backend.feature.exchange.repository.cassandra.buy.BuyOrderRepository;
import com.kmbbj.backend.feature.exchange.repository.cassandra.sell.SellOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExecutionAllMatchingOrderImpl implements ExecutionAllMatchingOrder {
    private final SellOrderRepository sellOrderRepository;
    private final BuyOrderRepository buyOrderRepository;

    public List<SellOrder> findEligibleSellOrders(Long coinId, BigDecimal price) {
        return sellOrderRepository.findAllByIdCoinIdAndIdPriceLessThanEqual(coinId, price);
    }

    public List<BuyOrder> findEligibleBuyOrders(Long coinId, BigDecimal price) {
        return buyOrderRepository.findAllByIdCoinIdAndIdPriceGreaterThanEqual(coinId, price);
    }

    @Transactional
    public List<String> matchOrders(String symbol, BigDecimal price) {
        List<String> matchedOrders = new ArrayList<>();

        return matchedOrders;
    }
}
