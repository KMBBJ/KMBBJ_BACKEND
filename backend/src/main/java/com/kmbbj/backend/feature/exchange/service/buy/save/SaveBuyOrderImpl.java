package com.kmbbj.backend.feature.exchange.service.buy.save;

import com.kmbbj.backend.feature.exchange.controller.request.OrderRequest;
import com.kmbbj.backend.feature.exchange.entity.cassandra.BuyOrder;
import com.kmbbj.backend.feature.exchange.repository.cassandra.buy.BuyOrderRepository;
import com.kmbbj.backend.feature.exchange.repository.postgre.TransactionRepository;
import com.kmbbj.backend.feature.exchange.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SaveBuyOrderImpl implements SaveBuyOrder {
    //카산드라 리파지토리
    private final BuyOrderRepository buyOrderRepository;
    //postgre 리파지토리
    private final TransactionRepository transactionRepository;



    public BuyOrder orderRequestToBuyOrder(OrderRequest orderRequest) {
        return BuyOrder.builder()
                .build();
    }
}