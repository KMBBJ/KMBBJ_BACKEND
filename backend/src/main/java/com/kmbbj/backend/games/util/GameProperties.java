package com.kmbbj.backend.games.util;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
@Component
public class GameProperties {

    @Value("${GAME_ROUND_DURATION_MINUTES}")
    private int gameRoundDurationMinutes;

    public int getGameRoundDurationMinutes() {
        return gameRoundDurationMinutes;
    }
}
