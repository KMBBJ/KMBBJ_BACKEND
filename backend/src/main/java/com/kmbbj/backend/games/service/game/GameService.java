package com.kmbbj.backend.games.service.game;

import com.kmbbj.backend.games.dto.GameStartDTO;
import com.kmbbj.backend.games.dto.GameStatusDTO;
import jakarta.transaction.Transactional;

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
