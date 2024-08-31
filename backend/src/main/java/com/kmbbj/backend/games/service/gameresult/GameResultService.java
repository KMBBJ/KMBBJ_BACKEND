package com.kmbbj.backend.games.service.gameresult;

import com.kmbbj.backend.games.dto.GameResultDTO;
import com.kmbbj.backend.games.entity.GameResult;

import java.util.List;

public interface GameResultService {
    List<GameResult> createGameResults(String encryptedGameId);

    List<GameResultDTO> getGameResults(String encryptedGameId);
}
