package com.kmbbj.backend.games.service.game;

import com.kmbbj.backend.games.dto.CurrentRoundDTO;
import com.kmbbj.backend.games.dto.GameStatusDTO;

import java.util.UUID;

public interface GameService {
    GameStatusDTO startGame(Long roomId);
    void endGame(String encryptedGameId);

    GameStatusDTO getGameStatus(String encryptedGameId);

    CurrentRoundDTO getCurrentRound(String encryptedGameId);

    boolean isUserAuthorizedForGame(String encryptedGameId);
}
