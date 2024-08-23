package com.kmbbj.backend.feature.exchange.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(name = "주문 취소시 사용하는 객체", description = "주문 취소시 사용하면 됩니다.")
@Getter
@Builder
public class CanselRequest {
    @Schema(name = "게임 Id")
    private Long transactoinId;
}