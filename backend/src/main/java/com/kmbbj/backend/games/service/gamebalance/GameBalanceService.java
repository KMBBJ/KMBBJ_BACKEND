package com.kmbbj.backend.games.service.gamebalance;



import com.kmbbj.backend.games.dto.GameBalanceDTO;
import com.kmbbj.backend.games.entity.Game;
import com.kmbbj.backend.games.entity.GameBalance;
import jakarta.transaction.Transactional;

import java.util.List;


public interface GameBalanceService {
    List<GameBalance> createGameBalance(Game game);

    GameBalanceDTO getGameBalance(Long userId);

    @Transactional
    List<GameBalance> deleteGameBalances(Game game);
}
