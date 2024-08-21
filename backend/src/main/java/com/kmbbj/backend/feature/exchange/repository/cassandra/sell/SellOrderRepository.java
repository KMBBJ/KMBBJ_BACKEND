package com.kmbbj.backend.feature.exchange.repository.cassandra.sell;

import com.kmbbj.backend.feature.exchange.entity.cassandra.SellOrder;
import com.kmbbj.backend.feature.exchange.entity.cassandra.key.OrderPrimaryKey;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.math.BigDecimal;
import java.util.List;

public interface SellOrderRepository extends CassandraRepository<SellOrder, OrderPrimaryKey> {
    List<SellOrder> findAllBySymbolAndPriceLessThanEqual(String symbol, BigDecimal price);
}
