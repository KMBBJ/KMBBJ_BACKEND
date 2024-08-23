package com.kmbbj.backend.feature.exchange.util;

import com.kmbbj.backend.feature.exchange.controller.request.OrderRequest;
import com.kmbbj.backend.feature.exchange.entity.TransactionStatus;
import com.kmbbj.backend.feature.exchange.entity.cassandra.BuyOrder;
import com.kmbbj.backend.feature.exchange.entity.cassandra.SellOrder;
import com.kmbbj.backend.feature.exchange.entity.cassandra.key.OrderPrimaryKey;
import com.kmbbj.backend.feature.exchange.entity.postgre.Transaction;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;

//주문에서 공통으로 사용하는 메서드를 모아둔 유틸
@Component
public class ExchangeDTOMapper {
    /**
     * OrderRequest 객체를 BuyOrder 엔티티로 변환
     * @param orderRequest orderRequest 주문 관련 정보를 담고 있는 요청 객체
     * @param transactionId transactionId 거래의 고유 ID
     * @return 변환된 BuyOrder 객체를 반환
     */
    public BuyOrder orderRequestToBuyOrder(OrderRequest orderRequest, Long transactionId) {
        OrderPrimaryKey orderPrimaryKey = OrderPrimaryKey.builder()
                .coinId(orderRequest.getCoinId())
                .price(orderRequest.getPrice())
                .timestamp(Instant.now())
                .build();

        return BuyOrder.builder()
                .id(orderPrimaryKey)
                .transactionId(transactionId)
                .build();
    }

    /**
     * OrderRequest 객체를 SellOrder 엔티티로 변환
     * @param orderRequest orderRequest 주문 관련 정보를 담고 있는 요청 객체
     * @param transactionId transactionId 거래의 고유 ID
     * @return 변환된 SellOrder 객체를 반환
     */
    public SellOrder orderRequestToSellOrder(OrderRequest orderRequest, Long transactionId) {
        OrderPrimaryKey orderPrimaryKey = OrderPrimaryKey.builder()
                .coinId(orderRequest.getCoinId())
                .price(orderRequest.getPrice())
                .timestamp(Instant.now())
                .build();

        return SellOrder.builder()
                .id(orderPrimaryKey)
                .transactionId(transactionId)
                .build();
    }

    /**
     * OrderRequest 객체를 Transaction 엔티티로 변환
     *
     * @param orderRequest orderRequest 주문 관련 정보를 담고 있는 요청 객체
     * @param balancesId balancesId 잔액의 고유 ID
     * @return 변환된 Transaction 객체를 반환
     */
    public Transaction orderRequestToTransaction(OrderRequest orderRequest, Long balancesId) {
        return Transaction.builder()
                .transactionType(orderRequest.getTransactionType())
                .quantity(orderRequest.getAmount())
                .price(orderRequest.getPrice())
                .createDate(LocalDateTime.now())
                .status(TransactionStatus.PENDING)
                .balancesId(balancesId)
                .gameId(orderRequest.getGameId())
                .coinId(orderRequest.getCoinId())
                .build();
    }
}