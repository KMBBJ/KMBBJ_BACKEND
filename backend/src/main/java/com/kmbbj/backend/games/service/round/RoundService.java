package com.kmbbj.backend.games.service.round;

import com.kmbbj.backend.games.dto.RoundRankingSimpleDTO;

import java.util.List;

public interface RoundService {

    boolean manageRounds(String encryptedGameId);
    List<List<RoundRankingSimpleDTO>> getRoundRankingsForGame(String encryptedGameId);
}