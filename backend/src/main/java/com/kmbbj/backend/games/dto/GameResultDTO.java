package com.kmbbj.backend.games.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameResultDTO {
    private String username; // 사용자 이름
    private Long totalProfit; // 총 수익 금액
    private Long totalLoss; // 총 손실 금액
    private Integer userRank; // 유저 순위
}
