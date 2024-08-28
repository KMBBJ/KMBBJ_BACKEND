package com.kmbbj.backend.feature.exchange.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(name = "사용자가 자산을 조회를 요청할때 보내는 아이디")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserAssetRequest {
    @Schema(name = "사용자 아이디")
    private Long userId;
}