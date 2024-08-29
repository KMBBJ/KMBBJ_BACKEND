package com.kmbbj.backend.feature.exchange.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "사용 가능한 자산 응답")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AvailableBuyFundsResponse {
    @Schema(description = "사용가능한 자산")
    private Long availableAsset;
}
