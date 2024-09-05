package com.kmbbj.backend.games.service.gameresult;

import com.kmbbj.backend.games.dto.GameResultDTO;
import com.kmbbj.backend.games.entity.GameResult;

import java.util.List;
import java.util.UUID;

public interface GameResultService {
    List<GameResult> createGameResults(UUID encryptedGameId);

    List<GameResultDTO> getGameResults(UUID encryptedGameId);
}
