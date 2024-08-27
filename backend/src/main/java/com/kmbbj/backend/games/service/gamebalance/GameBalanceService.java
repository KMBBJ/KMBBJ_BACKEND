package com.kmbbj.backend.games.service.gamebalance;


import com.kmbbj.backend.games.entity.Game;
import com.kmbbj.backend.games.entity.GameBalance;

import java.util.List;

public interface GameBalanceService {
    List<GameBalance> createGameBalance(Game game);
}
