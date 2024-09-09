package com.kmbbj.backend.feature.exchange.service.execution.matching;

import com.kmbbj.backend.feature.exchange.entity.TransactionStatus;
import com.kmbbj.backend.feature.exchange.entity.cassandra.BuyOrder;
import com.kmbbj.backend.feature.exchange.entity.cassandra.SellOrder;
import com.kmbbj.backend.feature.exchange.entity.postgre.Transaction;
import com.kmbbj.backend.feature.exchange.repository.cassandra.buy.BuyOrderRepository;
import com.kmbbj.backend.feature.exchange.repository.cassandra.sell.SellOrderRepository;
import com.kmbbj.backend.feature.exchange.repository.postgre.TransactionRepository;
import com.kmbbj.backend.feature.games.entity.CoinBalance;
import com.kmbbj.backend.feature.games.entity.GameBalance;
import com.kmbbj.backend.feature.games.repository.CoinBalanceRepository;
import com.kmbbj.backend.feature.games.repository.GameBalanceRepository;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//코인 가격이 변경됐을때 호출해야하는 메서드 구현 서비스
@Service
@RequiredArgsConstructor
public class ExecutionAllMatchingOrderImpl implements ExecutionAllMatchingOrder {
    //카산드라 판매
    private final SellOrderRepository sellOrderRepository;
    //카산드라 구매
    private final BuyOrderRepository buyOrderRepository;
    //postgre 주문 저장 로직
    private final TransactionRepository transactionRepository;
    //게임 계좌 리파지토리
    private final GameBalanceRepository gameBalanceRepository;
    //게임 코인 계좌
    private final CoinBalanceRepository coinBalanceRepository;

    /**
     * 주어진 코인 ID와 가격에 따라 판매 및 구매 주문을 매칭
     * 매칭된 주문의 트랜잭션 상태를 업데이트한 후,
     * PostgreSQL에 일괄 저장하고, Cassandra에서 해당 주문들을 삭제
     *
     * @param coinId 매칭할 코인의 ID
     * @param price 매칭 기준 가격
     */
    @Override
    @Transactional
    public void matchOrders(Long coinId, Long price) {
        // 매칭 가능한 판매 및 구매 주문을 조회
        List<SellOrder> eligibleSellOrders = findEligibleSellOrders(coinId, price);
        List<BuyOrder> eligibleBuyOrders = findEligibleBuyOrders(coinId, price);

        // 업데이트할 트랜잭션 리스트와 매칭된 주문 ID 리스트 초기화
        List<Transaction> transactionsToUpdate = new ArrayList<>();
        List<GameBalance> gameBalancesToUpdate = new ArrayList<>();
        List<CoinBalance> coinBalancesToUpdate = new ArrayList<>();

        // 매칭 로직: 모든 판매 주문과 구매 주문을 비교하여 매칭
        // 판매 및 구매 트랜잭션 상태를 COMPLETED로 업데이트


        for (SellOrder sellOrder : eligibleSellOrders) {
            Transaction sellTransaction = updateTransactionStatus(sellOrder);
            sumGameBalanceSeed(sellTransaction, gameBalancesToUpdate);

            // 업데이트할 트랜잭션 리스트에 추가
            transactionsToUpdate.add(sellTransaction);
        }

        for (BuyOrder buyOrder : eligibleBuyOrders) {
            Transaction buyTransaction = updateTransactionStatus(buyOrder);
            sumCoinBalanceCoin(buyTransaction, coinBalancesToUpdate);

            // 업데이트할 트랜잭션 리스트에 추가
            transactionsToUpdate.add(buyTransaction);
        }

        // PostgreSQL에 트랜잭션 일괄 업데이트
        transactionRepository.saveAll(transactionsToUpdate);

        // PostgreSQL에 게임 계좌와 코인 계좌 일괄 업데이트
        gameBalanceRepository.saveAll(gameBalancesToUpdate);
        coinBalanceRepository.saveAll(coinBalancesToUpdate);

        // Cassandra에서 매칭된 주문 삭제
        deleteMatchedOrders(eligibleSellOrders, eligibleBuyOrders);
    }

    /**
     * 판매가 체결 됐을때 게임 계좌의 잔액을 증가시키는 메서드
     * @param transaction 정보를 담고있는 거래 객체
     * @param gameBalances 업데이트할 게임 계좌 리스트
     */
    private void sumGameBalanceSeed(Transaction transaction, List<GameBalance> gameBalances) {
        GameBalance gameBalance = gameBalanceRepository.findById(transaction.getBalancesId()).orElseThrow(() -> new ApiException(ExceptionEnum.BALANCE_NOT_FOUND));
        gameBalance.setSeed(gameBalance.getSeed() + transaction.getTotalPrice());
        // 업데이트된 게임 계좌를 리스트에 추가
        gameBalances.add(gameBalance);
    }

    /**
     * 코인이 체결 됐을때 코인 계좌의 잔액을 증가시키는 메서드 계좌가 없으면 만든다.
     * @param transaction 정보를 담고있는 거래 객체
     * @param coinBalances 업데이트할 코인 계좌 리스트
     */
    private void sumCoinBalanceCoin(Transaction transaction, List<CoinBalance> coinBalances) {
        CoinBalance coinBalance = coinBalanceRepository.findCoinBalanceByGameBalanceIdAndCoinId(transaction.getBalancesId(), transaction.getCoinId()).orElseGet(() ->
                CoinBalance.builder()
                        .gameBalanceId(transaction.getBalancesId())
                        .coinId(transaction.getCoinId())
                        .quantity(BigDecimal.valueOf(0))
                        .build()
        );
        coinBalance.setQuantity(coinBalance.getQuantity().add(transaction.getQuantity()));
        // 업데이트된 코인 계좌를 리스트에 추가
        coinBalances.add(coinBalance);
    }

    /**
     * 주어진 판매 주문에 해당하는 트랜잭션의 상태를 업데이트하고 반환
     *
     * @param sellOrder 상태를 업데이트할 판매 주문
     * @return 업데이트된 트랜잭션 객체
     */
    private Transaction updateTransactionStatus(SellOrder sellOrder) {
        Transaction transaction = transactionRepository.findById(sellOrder.getId().getTransactionId())
                .orElseThrow(() -> new ApiException(ExceptionEnum.NOT_FOUND_TRANSACTION));

        // 트랜잭션 상태와 체결 날짜를 업데이트, 상태 업데이트
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setExecutionDate(LocalDateTime.now());
        return transaction;
    }

    /**
     * 주어진 구매 주문에 해당하는 트랜잭션의 상태를 업데이트하고 반환
     *
     * @param buyOrder 상태를 업데이트할 구매 주문
     * @return 업데이트된 트랜잭션 객체
     */
    private Transaction updateTransactionStatus(BuyOrder buyOrder) {
        Transaction transaction = transactionRepository.findById(buyOrder.getId().getTransactionId())
                .orElseThrow(() -> new ApiException(ExceptionEnum.NOT_FOUND_TRANSACTION));

        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setExecutionDate(LocalDateTime.now());
        return transaction;
    }

    /**
     * Cassandra에서 매칭된 판매 및 구매 주문을 삭제
     *
     * @param sellOrders 삭제할 판매 주문 리스트
     * @param buyOrders 삭제할 구매 주문 리스트
     */
    private void deleteMatchedOrders(List<SellOrder> sellOrders, List<BuyOrder> buyOrders) {
        sellOrderRepository.deleteAll(sellOrders);
        buyOrderRepository.deleteAll(buyOrders);
    }

    /**
     * 주어진 코인 ID와 가격 이하의 판매 주문을 조회
     *
     * @param coinId 코인 ID
     * @param price 매칭 기준 가격
     * @return 매칭 가능한 판매 주문 리스트
     */
    private List<SellOrder> findEligibleSellOrders(Long coinId, Long price) {
        return sellOrderRepository.findAllByIdCoinIdAndIdPriceLessThanEqual(coinId, price);
    }

    /**
     * 주어진 코인 ID와 가격 이상의 구매 주문을 조회
     *
     * @param coinId 코인 ID
     * @param price 매칭 기준 가격
     * @return 매칭 가능한 구매 주문 리스트
     */
    private List<BuyOrder> findEligibleBuyOrders(Long coinId, Long price) {
        return buyOrderRepository.findAllByIdCoinIdAndIdPriceGreaterThanEqual(coinId, price);
    }
}