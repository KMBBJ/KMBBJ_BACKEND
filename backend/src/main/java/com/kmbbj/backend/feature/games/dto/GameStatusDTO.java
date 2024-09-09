package com.kmbbj.backend.feature.games.dto;

import com.kmbbj.backend.feature.games.enums.GameStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter@Setter
public class GameStatusDTO {
    private GameStatus status;     // 게임의 현재 상태
    private int durationMinutes;     // 라운드 지속 시간 (분 단위)
    private List<RoundResultDTO> results; // 해당 라운드의 여러 결과
}
