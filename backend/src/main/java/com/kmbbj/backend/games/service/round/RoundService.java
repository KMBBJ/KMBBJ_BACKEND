package com.kmbbj.backend.games.service.round;

import com.kmbbj.backend.games.entity.Game;

public interface RoundService {
    void startNewRound(Game game);
    boolean isLastRound(Game game, int endRoundNumber);

}