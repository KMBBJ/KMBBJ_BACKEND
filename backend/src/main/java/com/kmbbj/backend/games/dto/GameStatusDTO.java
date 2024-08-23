package com.kmbbj.backend.games.dto;

import com.kmbbj.backend.games.entity.RoundResult;
import com.kmbbj.backend.games.enums.GameStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter@Setter
public class GameStatusDTO {
    private String gameId;           // 게임 ID
    private GameStatus status;     // 게임의 현재 상태
}
