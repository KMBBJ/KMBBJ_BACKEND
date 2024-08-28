package com.kmbbj.backend.feature.exchange.service.buy.availablefunds;

import com.kmbbj.backend.feature.exchange.controller.request.AvailableBuyFundsRequest;
import com.kmbbj.backend.feature.exchange.controller.response.AvailableBuyFundsResponse;
import com.kmbbj.backend.games.entity.GameBalance;
import com.kmbbj.backend.games.repository.GameBalanceRepository;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FindAvailableFundsImpl implements FindAvailableFunds {
    private final GameBalanceRepository gameBalanceRepository;

    /**
     * 사용자가 매수에 사용할 수 있는 자금을 조회
     *
     * @param request 사용자의 ID를 포함한 요청 객체
     * @return AvailableBuyFundsResponse 사용자가 매수에 사용할 수 있는 자금을 포함한 응답 객체
     * @throws ApiException 사용자의 게임 잔액 정보를 찾을 수 없는 경우 예외가 발생
     */
    @Override
    public AvailableBuyFundsResponse findAvailableFunds(AvailableBuyFundsRequest request) {
        GameBalance gameBalance = gameBalanceRepository.findByUserId(request.getUserId()).orElseThrow(() -> new ApiException(ExceptionEnum.BALANCE_NOT_FOUND));
        return AvailableBuyFundsResponse.builder()
                .availableAsset(gameBalance.getSeed())
                .build();
    }
}