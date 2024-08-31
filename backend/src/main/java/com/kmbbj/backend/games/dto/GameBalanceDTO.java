package com.kmbbj.backend.games.dto;

import lombok.Getter;
import lombok.Setter;

@Setter@Getter
public class GameBalanceDTO {
    private Long initialBalance;   // 초기 잔액 (시드머니)
    private Long currentBalance;   // 현재 잔액
    private Long orderAmount;      // 총 주문 금액
    private Long profitAmount;     // 총 수익 금액
    private Long lossAmount;       // 총 손실 금액
    private String symbol;         // 코인 심볼
    private Long price;            // 거래된 가격
}
