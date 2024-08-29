package com.kmbbj.backend.feature.exchange.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Schema(name = "사용자 보유 자산")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserAssetResponse {
    @Schema(description = "총 보유 자산")
    private BigDecimal totalEvaluationAmount;
    @Schema(description = "총 매수 금액")
    private Long totalPurchaseAmount;
    @Schema(description = "총 수익률")
    private BigDecimal totalProfitRate;
    @Schema(description = "보유 코인 정보")
    private List<CoinAssetResponse> coinAssets;
}
