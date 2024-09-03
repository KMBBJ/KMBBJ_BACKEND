package com.kmbbj.backend.games.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter@Getter
public class GameStartDTO {
    private String gameId; // 게임 ID
    private List<String> UserId; // 사용자 ID
}
