package com.kmbbj.backend.feature.exchange.controller.request;

import com.kmbbj.backend.feature.exchange.entity.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "주문 요청시 사용하면됩니다.")
@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
    @Schema(description = "거래 내용")
    private TransactionType transactionType;

    @Schema(description = "거래된 수량")
    private BigDecimal amount;

    @Schema(description = "거래된 가격")
    private Long price;

    @Schema(description = "게임 Id")
    private UUID gameId;

    @Schema(description = "암호화 게임Id")
    private String privateGameId;

    @Schema(description = "코인 Id")
    private Long coinId;

    @Schema(description = "주문한 사람 Id")
    private Long userId;

    @Schema(description = "주문 총 가격")
    private Long totalPrice;
}
