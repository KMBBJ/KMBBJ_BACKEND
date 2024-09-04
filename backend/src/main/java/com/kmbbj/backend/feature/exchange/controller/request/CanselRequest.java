package com.kmbbj.backend.feature.exchange.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "주문 취소시 사용하면 됩니다.")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CanselRequest {
    @Schema(description = "게임 Id")
    private Long transactionId;
}