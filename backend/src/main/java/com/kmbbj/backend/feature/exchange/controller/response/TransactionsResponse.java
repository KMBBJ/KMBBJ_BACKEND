package com.kmbbj.backend.feature.exchange.controller.response;

import com.kmbbj.backend.feature.exchange.entity.TransactionStatus;
import com.kmbbj.backend.feature.exchange.entity.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(name = "반환될 거래 객체")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionsResponse {
    @Schema(description = "거래 리스트 모음")
    private Long transactionId;

    @Schema(description = "거래 유형")
    private TransactionType transactionType;

    @Schema(description = "거래된 수량")
    private BigDecimal quantity;

    @Schema(description = "거래를 신청한 가격")
    private Long price;

    @Schema(description = "거래된 총가격")
    private Long totalPrice;

    @Schema(description = "거래 생성 날자")
    private LocalDateTime createDate;

    @Schema(description = "체결이 됐는지")
    private TransactionStatus status = TransactionStatus.PENDING;

    @Schema(description = "체결이 됐다면 체결된 날자")
    private LocalDateTime executionDate;

    @Schema(description = "코인 아이디")
    private Long coinId;

    @Schema(description = "코인 이름")
    private String symbol;
}
