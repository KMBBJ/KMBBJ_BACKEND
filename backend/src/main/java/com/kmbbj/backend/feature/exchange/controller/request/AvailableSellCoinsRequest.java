package com.kmbbj.backend.feature.exchange.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(name = "사용자가 사용할수 있는 코인이 얼마나 남았는지 불러오는 요청에 사용")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AvailableSellCoinsRequest {
    @Schema(name = "사용자 ID")
    private Long userId;

    @Schema(name = "코인 Id")
    private Long coinId;
}
