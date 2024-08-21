package com.kmbbj.backend.feature.exchange.repository.cassandra.buy;

import com.kmbbj.backend.feature.exchange.entity.cassandra.BuyOrder;
import com.kmbbj.backend.feature.exchange.entity.cassandra.key.OrderPrimaryKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface BuyOrderRepository extends CassandraRepository<BuyOrder, OrderPrimaryKey> {
    List<BuyOrder> findAllByIdSymbolAndIdPriceGreaterThanEqual(String symbol, BigDecimal price);
}
