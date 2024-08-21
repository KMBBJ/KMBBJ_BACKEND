package com.kmbbj.backend.feature.exchange.entity.cassandra;

import com.kmbbj.backend.feature.exchange.entity.cassandra.key.OrderPrimaryKey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

//카산드라 구매 테이블
@Table("buy_orders")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BuyOrder {
    //복합 키
    @PrimaryKey
    private OrderPrimaryKey id;

    //postgre랑 연동할 아이디.
    private Long orderId;
}
