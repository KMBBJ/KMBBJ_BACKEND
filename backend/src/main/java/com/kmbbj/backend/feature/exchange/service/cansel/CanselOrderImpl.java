package com.kmbbj.backend.feature.exchange.service.cansel;

import com.kmbbj.backend.feature.exchange.controller.request.CanselRequest;
import com.kmbbj.backend.feature.exchange.entity.TransactionStatus;
import com.kmbbj.backend.feature.exchange.entity.TransactionType;
import com.kmbbj.backend.feature.exchange.entity.postgre.Transaction;
import com.kmbbj.backend.feature.exchange.repository.cassandra.buy.BuyOrderRepository;
import com.kmbbj.backend.feature.exchange.repository.cassandra.sell.SellOrderRepository;
import com.kmbbj.backend.feature.exchange.repository.postgre.TransactionRepository;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//구매 주문을 취소하는 구현체
@Service
@RequiredArgsConstructor
public class CanselOrderImpl implements CanselOrder {
    //거래 리파지토리
    private final TransactionRepository transactionRepository;
    //카산드라 구매 리파지토리
    private final BuyOrderRepository buyOrderRepository;
    //카산드라 판매 리파지토리
    private final SellOrderRepository sellOrderRepository;

    /**
     * 주어진 요청에 따라 구매 주문을 취소함.
     *
     * @param canselRequest 취소 요청 데이터
     * @throws ApiException 거래가 없거나, 삭제 중 문제가 발생하면 예외를 던짐
     */
    @Override
    @Transactional
    public void canselOrder(CanselRequest canselRequest) {
        // 거래 ID로 거래를 조회
        Transaction transaction = transactionRepository.findById(canselRequest.getTransactoinId()).orElseThrow(
                () -> new ApiException(ExceptionEnum.NOT_FOUND_TRANSACTION)
        );

        // 거래 상태를 취소로 업데이트하고 저장
        transaction.setStatus(TransactionStatus.CANCELLED);
        transactionRepository.save(transaction);

        // 코인 ID를 기준으로 해당 거래 ID를 가진 구매 주문을 삭제
        try {
            if (transaction.getTransactionType().equals(TransactionType.BUY)) {
                buyOrderRepository.deleteByIdCoinIdAndTransactionId(transaction.getTransactionId(), transaction.getCoinId());
            } else if (transaction.getTransactionType().equals(TransactionType.SELL)) {
                sellOrderRepository.deleteByIdCoinIdAndTransactionId(transaction.getTransactionId(), transaction.getCoinId());
            } else {
                throw new ApiException(ExceptionEnum.NOT_FOUND_TRANSACTION_TYPE);
            }
        } catch (DataAccessException e) {
            throw new ApiException(ExceptionEnum.INTERNAL_SERVER_ERROR);
        }
    }
}