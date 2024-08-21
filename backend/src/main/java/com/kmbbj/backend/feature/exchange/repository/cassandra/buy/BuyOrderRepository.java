package com.kmbbj.backend.feature.exchange.repository.cassandra.buy;

import com.kmbbj.backend.feature.exchange.entity.cassandra.BuyOrder;
import com.kmbbj.backend.feature.exchange.entity.cassandra.key.OrderPrimaryKey;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.math.BigDecimal;
import java.util.List;

public interface BuyOrderRepository extends CassandraRepository<BuyOrder, OrderPrimaryKey> {
    List<BuyOrder> findAllBySymbolAndPriceGreaterThanEqual(String symbol, BigDecimal price);
}
