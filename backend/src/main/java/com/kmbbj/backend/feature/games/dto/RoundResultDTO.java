package com.kmbbj.backend.feature.games.dto;

import lombok.Getter;
import lombok.Setter;

@Getter@Setter
public class RoundResultDTO {
    private Long roundId; // 라운드 ID
    private int roundNumber; // 라운드 번호
    private Long resultId; // 라운드 결과 ID
    private String topBuyCoin; // 가장 많이 매수된 코인
    private String topBuyPercent; // 가장 많이 매수된 코인 비율 (%)
    private String topProfitCoin; // 가장 많이 수익된 코인
    private String topProfitPercent; // 가장 많이 수익된 코인 (%)
    private String topLossCoin; // 가장 많이 손실된 코인
    private String topLossPercent; // // 가장 많이 손실된 코인 (%)
}
