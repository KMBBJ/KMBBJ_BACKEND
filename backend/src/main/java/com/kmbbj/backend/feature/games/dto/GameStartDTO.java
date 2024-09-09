package com.kmbbj.backend.feature.games.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Setter@Getter
public class GameStartDTO {
    private UUID gameId; // 게임 ID
    private List<Long> UserId; // 사용자 ID
}
