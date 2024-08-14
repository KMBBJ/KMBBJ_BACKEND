package com.kmbbj.backend.games.service.game;

import com.kmbbj.backend.games.dto.CurrentRoundDTO;
import com.kmbbj.backend.games.dto.GameStatusDTO;
import com.kmbbj.backend.games.entity.Game;

public interface GameService {
    Game startGame(Long roomId);
    void endGame(Long roomId);
    GameStatusDTO getGameStatus(Long gameId);
    CurrentRoundDTO getCurrentRound(Long gameId);

}
