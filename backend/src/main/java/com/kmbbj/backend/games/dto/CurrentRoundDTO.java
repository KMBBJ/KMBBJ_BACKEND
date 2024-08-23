package com.kmbbj.backend.games.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CurrentRoundDTO {
    private Long gameId; // 게임 ID
    private int currentRoundNumber; // 라운드 번호
    private String gameStatus; // 게임 상태
}
