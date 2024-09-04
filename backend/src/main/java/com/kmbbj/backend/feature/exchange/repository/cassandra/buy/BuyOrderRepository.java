package com.kmbbj.backend.feature.exchange.repository.cassandra.buy;

import com.kmbbj.backend.feature.exchange.entity.cassandra.BuyOrder;
import com.kmbbj.backend.feature.exchange.entity.cassandra.key.OrderPrimaryKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BuyOrderRepository extends CassandraRepository<BuyOrder, OrderPrimaryKey> {
    List<BuyOrder> findAllByIdCoinIdAndIdPriceGreaterThanEqual(Long coinId, Long price);

    List<BuyOrder> findAllByIdCoinIdAndIdPriceAndIdTransactionId(Long coinId, Long price, Long transactionId);
}