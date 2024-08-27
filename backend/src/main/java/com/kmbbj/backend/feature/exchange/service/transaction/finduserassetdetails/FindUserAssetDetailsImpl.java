package com.kmbbj.backend.feature.exchange.service.transaction.finduserassetdetails;

import com.kmbbj.backend.feature.exchange.controller.response.CoinAssetResponse;
import com.kmbbj.backend.feature.exchange.controller.response.UserAssetResponse;
import com.kmbbj.backend.feature.exchange.repository.postgre.TransactionRepository;
import com.kmbbj.backend.games.entity.GameBalance;
import com.kmbbj.backend.games.repository.GameBalanceRepository;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FindUserAssetDetailsImpl implements FindUserAssetDetails{
    private final GameBalanceRepository gameBalanceRepository;
    private final TransactionRepository transactionRepository;

    /**
     * 사용자의 자산 상세 정보를 조회
     *
     * @param userId 자산 정보를 조회할 사용자 ID
     * @return UserAssetResponse 사용자 자산에 대한 총 평가 금액, 매수 금액, 수익률 및 각 코인에 대한 자산 정보를 포함하는 객체
     * @throws ApiException 사용자의 게임 잔액 정보를 찾을 수 없는 경우 예외가 발생
     */
    @Override
    public UserAssetResponse FindUserAssetDetails(Long userId){
        // 사용자의 게임 잔액 정보를 조회
        GameBalance gameBalance = gameBalanceRepository.findByUserId(userId).orElseThrow(() -> new ApiException(ExceptionEnum.BALANCE_NOT_FOUND));
        List<CoinAssetResponse> coinAssets = transactionRepository.findAllCoinAssets(gameBalance.getGameBalancesId()); // 게임 잔액 ID를 기반으로 모든 코인 자산 정보를 가져옵

        BigDecimal totalEvaluationAmount = BigDecimal.ZERO; // 총 평가 금액 초기화
        Long totalPurchaseAmount = 0L; // 총 매수 금액 초기화

        // 각 코인에 대한 자산 정보를 순회하며 총 평가 금액과 총 매수 금액을 계산
        for (CoinAssetResponse asset : coinAssets) {
            totalEvaluationAmount = totalEvaluationAmount.add(asset.getEvaluationAmount());
            totalPurchaseAmount += asset.getPurchaseAmount(); // 매수 금액 합산
        }

        // 전체 수익률을 계산
        BigDecimal totalProfitRate = calculateProfitRate(totalPurchaseAmount, totalEvaluationAmount);

        return new UserAssetResponse(totalEvaluationAmount, totalPurchaseAmount, totalProfitRate, coinAssets);
    }

    /**
     * 수익률을 계산
     *
     * @param purchaseAmount 총 매수 금액
     * @param evaluationAmount 현재 총 평가 금액
     * @return BigDecimal 수익률을 나타내는 백분율 값
     */
    private BigDecimal calculateProfitRate(Long purchaseAmount, BigDecimal evaluationAmount) {
        if (purchaseAmount == 0) {
            return BigDecimal.ZERO; // 매수 금액이 0인 경우 수익률은 0으로 반환
        }
        // 수익률 = (평가 금액 - 매수 금액) / 매수 금액 * 100
        return evaluationAmount.subtract(BigDecimal.valueOf(purchaseAmount))
                .divide(BigDecimal.valueOf(purchaseAmount), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}