package com.kmbbj.backend.games.service.roundresult;

import com.kmbbj.backend.games.dto.RoundResultDTO;

import java.util.List;
import java.util.UUID;

public interface RoundResultService {
    RoundResultDTO calculateRoundResult(UUID gameId, Long roundId);

    void saveRoundResult(RoundResultDTO roundResultDTO);

    List<RoundResultDTO> getCompletedRoundResultsForGame(String encryptedGameId);
}
