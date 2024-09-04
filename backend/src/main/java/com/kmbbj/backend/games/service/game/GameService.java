package com.kmbbj.backend.games.service.game;

import com.kmbbj.backend.games.dto.GameStartDTO;
import com.kmbbj.backend.games.dto.GameStatusDTO;

public interface GameService {
    GameStartDTO startGame(Long roomId);
    void endGame(String encryptedGameId);

    GameStatusDTO getGameStatus(String encryptedGameId);

    boolean isUserAuthorizedForGame(String encryptedGameId);

    boolean isGameInProgress(Long roomId);

    Long getUserParticipatingRoom();

    String getEncryptedGameIdForUser(Long userId);
}
