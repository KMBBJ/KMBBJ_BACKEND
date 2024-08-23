package com.kmbbj.backend.feature.exchange.util;

import com.kmbbj.backend.auth.repository.UserRepository;
import com.kmbbj.backend.charts.repository.coin.CoinRepository;
import com.kmbbj.backend.feature.exchange.controller.request.OrderRequest;
import com.kmbbj.backend.feature.exchange.entity.postgre.Transaction;
import com.kmbbj.backend.feature.exchange.repository.postgre.TransactionRepository;
import com.kmbbj.backend.games.repository.GameBalanceRepository;
import com.kmbbj.backend.games.repository.GameRepository;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SaveOrderUtil {
    //카산드라 리파지토리
    private final TransactionRepository transactionRepository;
    //postgre 리파지토리
    private final ExchangeDTOMapper exchangeDTOMapper;
    //사용자 확인용 리파지토리
    private final UserRepository userRepository;
    //코인 확인용 리파지토리
    private final CoinRepository coinRepository;
    //게임 확인용 리파지토리
    private final GameRepository gameRepository;
    //게임안 계좌 리파지토리
    private final GameBalanceRepository gameBalanceRepository;

    /**
     * OrderRequest로부터 사용자, 코인, 게임 계좌를 검증하고, Transaction을 생성하는 메서드.
     *
     * 이 메서드는 다음 작업을 수행함:
     * 1. 사용자 ID로 사용자가 존재하는지 확인. 존재하지 않으면 USER_NOT_FOUND 예외 발생.
     * 2. 코인 ID로 해당 코인이 존재하는지 확인. 존재하지 않으면 NOT_FOUND_SYMBOL 예외 발생.
     * 3. 게임 계좌를 조회하여 사용자의 게임 계좌 ID를 확인. 계좌가 없으면 BALANCE_NOT_FOUND 예외 발생.
     * 4. 검증을 통과한 후, OrderRequest와 게임 계좌 ID를 사용해 새로운 Transaction 객체를 생성하고 저장.
     * 5. 생성된 Transaction의 ID를 반환.
     *
     * @param orderRequest 주문 요청 객체
     * @return 생성된 Transaction의 ID
     * @throws ApiException 유효하지 않은 사용자, 코인, 게임 계좌일 때 발생하는 예외
     */
    public Long validateAndCreateTransaction(OrderRequest orderRequest) {
        // 사용자 있는지 확인
        userRepository.findById(orderRequest.getUserId()).orElseThrow(() -> new ApiException(ExceptionEnum.USER_NOT_FOUND));
        // 코인 있는지 확인
        coinRepository.findById(orderRequest.getCoinId()).orElseThrow(() -> new ApiException(ExceptionEnum.NOT_FOUND_SYMBOL));
        //게임 있는지 확인
        //gameRepository.findById(orderRequest.getGameId()).orElseThrow(() -> new ApiException(ExceptionEnum.GAME_NOT_FOUND));
        // 게임 안 계좌 조회
        Long gameBalanceId = gameBalanceRepository.findIdByUserId(orderRequest.getUserId()).orElseThrow(() -> new ApiException(ExceptionEnum.BALANCE_NOT_FOUND));

        Transaction transaction = exchangeDTOMapper.orderRequestToTransaction(orderRequest, gameBalanceId);
        transactionRepository.save(transaction);
        return transaction.getTransactionId();
    }
}
