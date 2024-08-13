package com.kmbbj.backend.games.service.round;

import com.kmbbj.backend.games.entity.Game;
import com.kmbbj.backend.games.entity.Round;
import com.kmbbj.backend.games.enums.GameStatus;
import com.kmbbj.backend.games.repository.GameRepository;
import com.kmbbj.backend.games.repository.RoundRepository;
import com.kmbbj.backend.games.service.transaction.CoinSummaryService;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoundServiceImpl implements RoundService {

    @Value("${game.round.duration.minutes:1440}")
    private int roundDurationMinutes;

    private final RoundRepository roundRepository;
    private final GameRepository gameRepository;
    private final CoinSummaryService coinSummaryService;


    /** 새로운 라운드 시작
     *
     * @param game
     */
    @Override
    @Transactional
    public void startNewRound(Game game) {
        Round currentRound = roundRepository.findFirstByGameOrderByRoundNumberDesc(game)
                .orElseThrow(() -> new ApiException(ExceptionEnum.ROUND_NOT_FOUND));

        // 라운드 종료 시 데이터 분석
        coinSummaryService.getRoundResult(currentRound.getRoundId());

        if (roundRepository.existsByGameAndRoundNumber(game, currentRound.getRoundNumber() + 1)) {
            throw new ApiException(ExceptionEnum.DUPLICATE_ROUND);
        }

        // 새로운 라운드 생성
        Round newRound = new Round();
        newRound.setGame(game);
        newRound.setRoundNumber(currentRound.getRoundNumber() + 1);
        newRound.setDurationMinutes(roundDurationMinutes);
        roundRepository.save(newRound);

        // 게임 상태 업데이트
        game.setGameStatus(GameStatus.ACTIVE);
        gameRepository.save(game);
    }


    /** 게임이 마지막 라운드에 도달 했는지 확인
     *
     * @param game 확인할 게임 객체
     * @param endRoundNumber 게임이 종료될 라운드 번호
     * @return 마지막 라운드 도달하면 True, 그렇지 않으면 False 반환
     */
    @Override
    public boolean isLastRound(Game game, int endRoundNumber) {

        Round latestRound = roundRepository.findFirstByGameOrderByRoundNumberDesc(game)
                .orElseThrow(() -> new ApiException(ExceptionEnum.ROUND_NOT_FOUND));

        return latestRound.getRoundNumber() >= endRoundNumber;
    }
}
