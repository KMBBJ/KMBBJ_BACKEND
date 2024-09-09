package com.kmbbj.backend.feature.games.service.roundresult;

import com.kmbbj.backend.feature.games.dto.RoundResultDTO;

import java.util.List;
import java.util.UUID;

public interface RoundResultService {
    RoundResultDTO calculateRoundResult(UUID gameId, Long roundId);

    void saveRoundResult(RoundResultDTO roundResultDTO);

    List<RoundResultDTO> getCompletedRoundResultsForGame(UUID encryptedGameId);
}
