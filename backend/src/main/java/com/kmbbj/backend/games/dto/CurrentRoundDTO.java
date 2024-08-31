package com.kmbbj.backend.games.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CurrentRoundDTO {
    private int roundNumber; // 라운드 번호
    private int durationMinutes; // 라운드 지속 시간
    private String gameStatus; // 게임 상태
}
