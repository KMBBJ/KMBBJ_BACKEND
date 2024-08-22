package com.kmbbj.backend.feature.exchange.controller.request;

import com.kmbbj.backend.feature.exchange.entity.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(name = "주문 요청시 사용하는 객체", description = "주문 요청시 사용하면됩니다.")
public class OrderRequest {
    @Schema(name = "거래 내용")
    private TransactionType transactionType;

    @Schema(name = "거래된 수량")
    private BigDecimal amount;

    @Schema(name = "거래된 가격")
    private BigDecimal price;

    @Schema(name = "게임 Id")
    private String orderId;

    @Schema(name = "코인 Id")
    private String coinId;
}
