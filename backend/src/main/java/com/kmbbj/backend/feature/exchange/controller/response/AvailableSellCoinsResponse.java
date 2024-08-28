package com.kmbbj.backend.feature.exchange.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Schema(name = "사용 가능한 코인을 보여줄 응답")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AvailableSellCoinsResponse {
    @Schema(name = "사용 가능한 코인")
    private BigDecimal availableCoin;
}