package com.kmbbj.backend.feature.exchange.repository.cassandra.sell;

import com.kmbbj.backend.feature.exchange.entity.cassandra.SellOrder;
import com.kmbbj.backend.feature.exchange.entity.cassandra.key.OrderPrimaryKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SellOrderRepository extends CassandraRepository<SellOrder, OrderPrimaryKey> {
    List<SellOrder> findAllByIdCoinIdAndIdPriceLessThanEqual(Long coinId, Long price);
    void deleteByIdCoinIdAndTransactionId(Long coinId, Long transactionId);
}