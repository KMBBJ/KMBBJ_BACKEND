package com.kmbbj.backend.feature.exchange.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Schema(name = "코인 보유 자산을 보여줄 객체")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CoinAssetResponse {
    @Schema(name = "코인 심볼 이름")
    private String coinSymbol;
    @Schema(name = "보유 코인 갯수", description = "거래를 신청해 놨을 경우 코인 갯수가 그보다 작게 나타남")
    private BigDecimal quantity;
    @Schema(name = "매수 금액")
    private Long purchaseAmount;
    @Schema(name = "평가 긍맥")
    private BigDecimal evaluationAmount;
    @Schema(name = "수익률")
    private BigDecimal profitRate;
}
