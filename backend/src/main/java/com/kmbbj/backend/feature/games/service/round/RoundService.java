package com.kmbbj.backend.feature.games.service.round;

import com.kmbbj.backend.feature.games.dto.CurrentRoundDTO;
import com.kmbbj.backend.feature.games.dto.RoundRankingSimpleDTO;
import com.kmbbj.backend.feature.games.entity.Game;

import java.util.List;
import java.util.UUID;

public interface RoundService {

    boolean manageRounds(UUID encryptedGameId);

    CurrentRoundDTO startNewRound(Game game);

    CurrentRoundDTO endCurrentAndStartNextRound(UUID gameId);

    List<List<RoundRankingSimpleDTO>> getRoundRankingsForGame(UUID gameId);

    List<RoundRankingSimpleDTO> getCurrentRoundRankingsForGame(UUID gameId);
}