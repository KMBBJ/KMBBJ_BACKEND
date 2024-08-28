package com.kmbbj.backend.feature.exchange.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "사용자가 사용할수 있는 잔액이 얼마나 남았는지 불러오는 요청에 사용")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AvailableBuyFundsRequest {
    @Schema(description = "사용자 ID")
    private Long userId;
}
