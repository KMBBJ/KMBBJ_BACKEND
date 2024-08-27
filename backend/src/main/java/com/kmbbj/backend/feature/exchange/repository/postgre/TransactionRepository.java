package com.kmbbj.backend.feature.exchange.repository.postgre;

import com.kmbbj.backend.feature.exchange.controller.response.TransactionsResponse;
import com.kmbbj.backend.feature.exchange.entity.postgre.Transaction;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    //코인 symbol을 가져오혀 한번에 응답으로 변환하는 방식의 쿼리
    @Query("SELECT new com.kmbbj.backend.feature.exchange.controller.response.TransactionsResponse(" +
            "t.transactionId, t.transactionType, t.quantity, t.price, t.totalPrice, t.createDate, " +
            "t.status, t.executionDate, t.coinId, c.symbol) " +
            "FROM Transaction t " +
            "JOIN Coin c ON t.coinId = c.coinId " +
            "WHERE t.balancesId = :balancesId")
    List<TransactionsResponse> findAllByBalancesIdWithCoinSymbol(@Param("balancesId") Long balancesId);
}