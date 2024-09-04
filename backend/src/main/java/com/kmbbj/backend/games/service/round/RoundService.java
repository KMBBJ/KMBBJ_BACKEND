package com.kmbbj.backend.games.service.round;

import com.kmbbj.backend.games.dto.CurrentRoundDTO;
import com.kmbbj.backend.games.dto.RoundRankingSimpleDTO;
import com.kmbbj.backend.games.entity.Game;
import jakarta.transaction.Transactional;

import java.util.List;

public interface RoundService {

    boolean manageRounds(String encryptedGameId);

    CurrentRoundDTO startNewRound(Game game);

    @Transactional
    CurrentRoundDTO endCurrentAndStartNextRound(String encryptedGameId);

    List<List<RoundRankingSimpleDTO>> getRoundRankingsForGame(String encryptedGameId);

    List<RoundRankingSimpleDTO> getCurrentRoundRankingsForGame(String encryptedGameId);
}