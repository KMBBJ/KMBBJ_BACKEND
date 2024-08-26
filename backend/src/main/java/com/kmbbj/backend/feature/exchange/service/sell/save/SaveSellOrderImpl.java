package com.kmbbj.backend.feature.exchange.service.sell.save;

import com.kmbbj.backend.auth.repository.UserRepository;
import com.kmbbj.backend.charts.repository.coin.CoinRepository;
import com.kmbbj.backend.feature.exchange.controller.request.OrderRequest;
import com.kmbbj.backend.feature.exchange.entity.postgre.Transaction;
import com.kmbbj.backend.feature.exchange.repository.cassandra.sell.SellOrderRepository;
import com.kmbbj.backend.feature.exchange.repository.postgre.TransactionRepository;
import com.kmbbj.backend.feature.exchange.util.ExchangeDTOMapper;
import com.kmbbj.backend.games.entity.CoinBalance;
import com.kmbbj.backend.games.repository.CoinBalanceRepository;
import com.kmbbj.backend.games.repository.GameBalanceRepository;
import com.kmbbj.backend.games.repository.GameRepository;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SaveSellOrderImpl implements SaveSellOrder {
    //카산드라 판매 기록 리파지토리
    private final SellOrderRepository sellOrderRepository;
    //DTO 관리 전용 Util
    private final ExchangeDTOMapper exchangeDTOMapper;
    //사용자 확인용 리파지토리
    private final UserRepository userRepository;
    //코인 확인용 리파지토리
    private final CoinRepository coinRepository;
    //게임 확인용 리파지토리
    private final GameRepository gameRepository;
    //게임안 계좌 리파지토리
    private final GameBalanceRepository gameBalanceRepository;
    //거래로그 리파지토리
    private final TransactionRepository transactionRepository;
    //게임안 코인 지갑
    private final CoinBalanceRepository coinBalanceRepository;


    @Override
    @Transactional
    public void saveSellOrder(OrderRequest orderRequest) {
        // 사용자 있는지 확인
        userRepository.findById(orderRequest.getUserId()).orElseThrow(() -> new ApiException(ExceptionEnum.USER_NOT_FOUND));
        // 코인 있는지 확인
        coinRepository.findById(orderRequest.getCoinId()).orElseThrow(() -> new ApiException(ExceptionEnum.NOT_FOUND_SYMBOL));
        //게임 있는지 확인
        //gameRepository.findById(orderRequest.getGameId()).orElseThrow(() -> new ApiException(ExceptionEnum.GAME_NOT_FOUND));
        // 게임 안 계좌 조회
        Long gameBalanceId = gameBalanceRepository.findIdByUserId(orderRequest.getUserId()).orElseThrow(() -> new ApiException(ExceptionEnum.BALANCE_NOT_FOUND));
        // 게임안 코인 조회
        CoinBalance coinBalance = coinBalanceRepository.findCoinBalanceByGameBalanceIdAndCoinId(gameBalanceId, orderRequest.getCoinId()).orElseThrow(() -> new ApiException(ExceptionEnum.COIN_BALANCE_NOT_FOUND));

        //판매할수 있는 코인을 가지고 있는지 확인후 기록
        if (orderRequest.getAmount().compareTo(coinBalance.getQuantity()) <= 0) {
            // orderRequest.getAmount()가 coinBalance.getQuantity()보다 작거나 같은 경우 코인을 빼고 주문에 저장
            coinBalance.setQuantity(coinBalance.getQuantity().subtract(orderRequest.getAmount()));
            coinBalanceRepository.save(coinBalance);
        } else {
            // orderRequest.getAmount()가 coinBalance.getQuantity()보다 큰 경우 코인이 부족합니다.
            throw new ApiException(ExceptionEnum.NOT_ENOUGH_COIN);
        }

        //거래 로그 기록
        Transaction transaction = exchangeDTOMapper.orderRequestToTransaction(orderRequest, gameBalanceId);
        transactionRepository.save(transaction);

        //카산드라에 등록
        try {
            sellOrderRepository.save(
                    exchangeDTOMapper.orderRequestToSellOrder(orderRequest, transaction.getTransactionId())
            );
        } catch (DataAccessException e) {
            throw new ApiException(ExceptionEnum.CASSANDRA_SAVE_EXCEPTION);
        }
    }
}