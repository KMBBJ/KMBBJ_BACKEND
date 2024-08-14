package com.kmbbj.backend.games.service.transaction;

import com.kmbbj.backend.games.dto.RoundResultDTO;



public interface CoinSummaryService  {
    void recordRoundResult(RoundResultDTO result);
    RoundResultDTO getRoundResult(Long roundId);

}
