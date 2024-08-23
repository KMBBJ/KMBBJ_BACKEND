package com.kmbbj.backend.feature.exchange.entity.cassandra.key;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@PrimaryKeyClass
@Getter
@Builder
public class OrderPrimaryKey implements Serializable {
    @PrimaryKeyColumn(name = "coin_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private Long coinId;

    @PrimaryKeyColumn(name = "price", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private BigDecimal price;

    @PrimaryKeyColumn(name = "timestamp", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    private Instant timestamp;
}