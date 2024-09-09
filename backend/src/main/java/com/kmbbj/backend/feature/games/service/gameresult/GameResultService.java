package com.kmbbj.backend.feature.games.service.gameresult;

import com.kmbbj.backend.feature.games.dto.GameResultDTO;
import com.kmbbj.backend.feature.games.entity.GameResult;

import java.util.List;
import java.util.UUID;

public interface GameResultService {
    List<GameResult> createGameResults(UUID encryptedGameId);

    List<GameResultDTO> getGameResults(UUID encryptedGameId);
}
