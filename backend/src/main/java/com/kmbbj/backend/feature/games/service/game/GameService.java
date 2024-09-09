package com.kmbbj.backend.feature.games.service.game;

import com.kmbbj.backend.feature.games.dto.GameStartDTO;
import com.kmbbj.backend.feature.games.dto.GameStatusDTO;

import java.util.UUID;

public interface GameService {
    GameStartDTO startGame(Long roomId);

    void endGame(UUID gameId);

    GameStatusDTO getGameStatus(UUID gameId);

    boolean isUserAuthorizedForGame(UUID gameId);

    boolean isGameInProgress(Long roomId);

    Long getUserParticipatingRoom();

    UUID getGameIdForUser(Long userId);
}
