package com.kmbbj.backend.feature.exchange.service.buy.save;

import com.kmbbj.backend.auth.repository.UserRepository;
import com.kmbbj.backend.charts.repository.coin.CoinRepository;
import com.kmbbj.backend.feature.exchange.controller.request.OrderRequest;
import com.kmbbj.backend.feature.exchange.entity.postgre.Transaction;
import com.kmbbj.backend.feature.exchange.repository.cassandra.buy.BuyOrderRepository;
import com.kmbbj.backend.feature.exchange.repository.postgre.TransactionRepository;
import com.kmbbj.backend.feature.exchange.util.ExchangeDTOMapper;
import com.kmbbj.backend.games.entity.GameBalance;
import com.kmbbj.backend.games.repository.GameBalanceRepository;
import com.kmbbj.backend.games.repository.GameRepository;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataAccessException;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class SaveBuyOrderImpl implements SaveBuyOrder {
    //카산드라 구매 기록 리파지토리
    private final BuyOrderRepository buyOrderRepository;

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

    @Override
    @Transactional
    public void saveBuyOrder(OrderRequest orderRequest) {
        // 사용자 있는지 확인
        userRepository.findById(orderRequest.getUserId()).orElseThrow(() -> new ApiException(ExceptionEnum.USER_NOT_FOUND));
        // 코인 있는지 확인
        coinRepository.findById(orderRequest.getCoinId()).orElseThrow(() -> new ApiException(ExceptionEnum.NOT_FOUND_SYMBOL));
        //게임 있는지 확인
        //gameRepository.findById(orderRequest.getGameId()).orElseThrow(() -> new ApiException(ExceptionEnum.GAME_NOT_FOUND));
        // 게임 안 계좌 조회
        GameBalance gameBalance = gameBalanceRepository.findByUserId(orderRequest.getUserId()).orElseThrow(() -> new ApiException(ExceptionEnum.BALANCE_NOT_FOUND));

        //주문을 처리할수 있는 금액이있는지 확인
        if (orderRequest.getTotalPrice() <= gameBalance.getSeed()) {
            // 총 주문 금액이 가지고있는 총액보다 작거나 같을 경우 돈일 빼고 저장
            gameBalance.setSeed(gameBalance.getSeed() - orderRequest.getTotalPrice());
            gameBalanceRepository.save(gameBalance);
        } else {
            // 아닐시 돈이 부족합니다.
            throw new ApiException(ExceptionEnum.NOT_ENOUGH_MONEY);
        }

        //거래를 기록
        Transaction transaction = exchangeDTOMapper.orderRequestToTransaction(orderRequest, gameBalance.getGameBalancesId());
        transactionRepository.save(transaction);

        //거래를 카산드라에 등록
        try {
            buyOrderRepository.save(
                    exchangeDTOMapper.orderRequestToBuyOrder(orderRequest, transaction.getTransactionId())
            );
        } catch (DataAccessException e) {
            throw new ApiException(ExceptionEnum.CASSANDRA_SAVE_EXCEPTION);
        }
    }
}