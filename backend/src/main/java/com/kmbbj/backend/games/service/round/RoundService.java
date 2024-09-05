package com.kmbbj.backend.games.service.round;

import com.kmbbj.backend.games.dto.CurrentRoundDTO;
import com.kmbbj.backend.games.dto.RoundRankingSimpleDTO;
import com.kmbbj.backend.games.entity.Game;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

public interface RoundService {

    boolean manageRounds(UUID encryptedGameId);

    CurrentRoundDTO startNewRound(Game game);

    CurrentRoundDTO endCurrentAndStartNextRound(UUID gameId);

    List<List<RoundRankingSimpleDTO>> getRoundRankingsForGame(UUID gameId);

    List<RoundRankingSimpleDTO> getCurrentRoundRankingsForGame(UUID gameId);
}