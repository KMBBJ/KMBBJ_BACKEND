package com.kmbbj.backend.games.dto;

import com.kmbbj.backend.games.entity.RoundResult;
import com.kmbbj.backend.games.enums.GameStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter@Setter
public class GameStatusDTO {
    private Long gameId;           // 게임 ID
    private GameStatus status;     // 게임의 현재 상태
    private List<RoundResultDTO> rounds; // 게임의 라운드 결과 목록
}
