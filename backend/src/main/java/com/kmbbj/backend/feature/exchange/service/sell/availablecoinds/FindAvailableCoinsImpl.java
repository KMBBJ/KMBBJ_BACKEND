package com.kmbbj.backend.feature.exchange.service.sell.availablecoinds;

import com.kmbbj.backend.feature.exchange.controller.request.AvailableSellCoinsRequest;
import com.kmbbj.backend.feature.exchange.controller.response.AvailableSellCoinsResponse;
import com.kmbbj.backend.games.entity.CoinBalance;
import com.kmbbj.backend.games.entity.GameBalance;
import com.kmbbj.backend.games.repository.CoinBalanceRepository;
import com.kmbbj.backend.games.repository.GameBalanceRepository;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FindAvailableCoinsImpl implements FindAvailableCoins {
    private final CoinBalanceRepository coinBalanceRepository;
    private final GameBalanceRepository gameBalanceRepository;

    /**
     * 사용자가 매도할 수 있는 코인의 수량을 조회
     *
     * @param request 사용자의 ID와 매도할 코인의 ID를 포함한 요청 객체
     * @return AvailableSellCoinsResponse 사용자가 매도할 수 있는 코인 수량을 포함한 응답 객체
     * @throws ApiException 사용자의 게임 잔액 정보를 찾을 수 없거나, 특정 코인의 잔액 정보를 찾을 수 없는 경우 예외가 발생
     */
    @Override
    public AvailableSellCoinsResponse findAvailableCoins(AvailableSellCoinsRequest request) {
        GameBalance gameBalance = gameBalanceRepository.findByUserId(request.getUserId()).orElseThrow(() -> new ApiException(ExceptionEnum.BALANCE_NOT_FOUND));
        CoinBalance coinBalance = coinBalanceRepository.findCoinBalanceByGameBalanceIdAndCoinId(gameBalance.getGameBalancesId(), request.getCoinId()).orElseThrow(() -> new ApiException(ExceptionEnum.COIN_BALANCE_NOT_FOUND));

        return AvailableSellCoinsResponse.builder()
                .availableCoin(coinBalance.getQuantity())
                .build();
    }
}
