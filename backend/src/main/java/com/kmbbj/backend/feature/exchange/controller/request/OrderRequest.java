package com.kmbbj.backend.feature.exchange.controller.request;

import com.kmbbj.backend.feature.exchange.entity.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(name = "주문 요청시 사용하는 객체", description = "주문 요청시 사용하면됩니다.")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
    @Schema(name = "거래 내용")
    private TransactionType transactionType;

    @Schema(name = "거래된 수량")
    private BigDecimal amount;

    @Schema(name = "거래된 가격")
    private Long price;

    @Schema(name = "게임 Id")
    private UUID gameId;

    @Schema(name = "코인 Id")
    private Long coinId;

    @Schema(name = "주문한 사람 Id")
    private Long userId;

    @Schema(name = "주문 총 가격")
    private Long totalPrice;
}
