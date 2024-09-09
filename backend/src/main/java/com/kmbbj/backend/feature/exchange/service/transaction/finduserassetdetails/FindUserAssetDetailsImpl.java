package com.kmbbj.backend.feature.exchange.service.transaction.finduserassetdetails;

import com.kmbbj.backend.feature.exchange.controller.response.CoinAssetResponse;
import com.kmbbj.backend.feature.exchange.controller.response.UserAssetResponse;
import com.kmbbj.backend.feature.exchange.repository.postgre.TransactionRepository;
import com.kmbbj.backend.feature.games.entity.GameBalance;
import com.kmbbj.backend.feature.games.repository.GameBalanceRepository;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
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
    public UserAssetResponse findUserAssetDetails(Long userId){
        // 사용자의 게임 잔액 정보를 조회
        GameBalance gameBalance = gameBalanceRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(ExceptionEnum.BALANCE_NOT_FOUND));

        List<Object[]> rawAssets = transactionRepository.findAllCoinAssets(gameBalance.getGameBalancesId());

        BigDecimal totalEvaluationAmount = BigDecimal.ZERO;
        Long totalPurchaseAmount = 0L;
        List<CoinAssetResponse> coinAssets = new ArrayList<>();

        for (Object[] rawAsset : rawAssets) {
            String coinSymbol = (String) rawAsset[0];
            BigDecimal quantity = (BigDecimal) rawAsset[1];
            Long totalPrice = (Long) rawAsset[2];
            BigDecimal currentPrice = BigDecimal.valueOf((Double) rawAsset[3]);

            // 평가 금액 계산
            BigDecimal evaluationAmount = quantity.multiply(currentPrice);

            // 매수 금액 합산
            totalPurchaseAmount += totalPrice;

            // 코인 자산 정보 생성
            CoinAssetResponse assetResponse = new CoinAssetResponse(
                    coinSymbol,
                    quantity,
                    totalPrice,
                    evaluationAmount,
                    BigDecimal.ZERO // 초기값, 나중에 수익률을 계산
            );

            coinAssets.add(assetResponse);

            // 총 평가 금액 합산
            totalEvaluationAmount = totalEvaluationAmount.add(evaluationAmount);
        }

        // 전체 수익률 계산
        BigDecimal totalProfitRate = calculateProfitRate(totalPurchaseAmount, totalEvaluationAmount);

        // 각 코인의 수익률 계산
        for (CoinAssetResponse asset : coinAssets) {
            BigDecimal profitRate = calculateProfitRate(asset.getPurchaseAmount(), asset.getEvaluationAmount());
            asset.setProfitRate(profitRate);
        }
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