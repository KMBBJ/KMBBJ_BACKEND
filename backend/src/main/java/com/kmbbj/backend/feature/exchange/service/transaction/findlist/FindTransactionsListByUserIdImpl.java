package com.kmbbj.backend.feature.exchange.service.transaction.findlist;

import com.kmbbj.backend.auth.repository.UserRepository;
import com.kmbbj.backend.feature.exchange.controller.request.TransactionsRequest;
import com.kmbbj.backend.feature.exchange.controller.response.TransactionsResponse;
import com.kmbbj.backend.feature.exchange.repository.postgre.TransactionRepository;
import com.kmbbj.backend.games.entity.GameBalance;
import com.kmbbj.backend.games.repository.GameBalanceRepository;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FindTransactionsListByUserIdImpl implements FindTransactionsByUserId {
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final GameBalanceRepository gameBalanceRepository;

    /**
     * 주어진 사용자 ID를 기반으로 해당 사용자의 거래 내역을 조회
     *
     * @param transactionsRequest 거래 내역을 조회할 사용자 ID를 포함한 요청 객체.
     * @return 거래 내역 및 코인 심볼 정보를 포함한 TransactionsResponse 객체 리스트.
     * @throws ApiException 사용자 또는 연관된 게임 잔액을 찾을 수 없는 경우 예외가 발생
     */
    @Override
    @Transactional(readOnly = true)
    public List<TransactionsResponse> getTransactionsByUserId(TransactionsRequest transactionsRequest) {
        // 사용자 있는지 확인
        userRepository.findById(transactionsRequest.getUserId()).orElseThrow(() -> new ApiException(ExceptionEnum.USER_NOT_FOUND));
        // 게임 계좌가 있는지 확인
        GameBalance gameBalance = gameBalanceRepository.findByUserId(transactionsRequest.getUserId()).orElseThrow(() -> new ApiException(ExceptionEnum.BALANCE_NOT_FOUND));

        // 거래 내역 및 코인 심볼 정보 조회
        return transactionRepository.findAllByBalancesIdWithCoinSymbol(gameBalance.getGameBalancesId());
    }
}