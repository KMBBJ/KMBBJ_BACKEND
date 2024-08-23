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
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoundServiceImpl implements RoundService {


    private final RoundRepository roundRepository;
    private final GameRepository gameRepository;
    private final CoinSummaryService coinSummaryService;



    /** 새로운 라운드 시작
     *
     * @param game 현재 진행 중인 게임 객체
     * @throws ApiException 공통 예외
     */
    @Override
    @Transactional
    public void startNewRound(Game game) {
        if (game == null) {
            throw new ApiException(ExceptionEnum.GAME_NOT_FOUND);
        }

        // 현재 라운드 중인 라운드 조회
        Round currentRound = roundRepository.findFirstByGameOrderByRoundNumberDesc(game)
                .orElseThrow(() -> new ApiException(ExceptionEnum.ROUND_NOT_FOUND));


        // 현재 라운드의 결과 분석
//        if (currentRound.getRoundNumber() > 1) {
//            try {
//                coinSummaryService.getRoundResult(currentRound.getRoundId());
//            } catch (ApiException e) {
//                if (!e.getException().equals(ExceptionEnum.ROUND_RESULT_NOT_FOUND)) {
//                    throw e;
//                }
//            }
//        }

        // 다음 라운드 번호가 이미 존재하는지 확인 여부
        if (roundRepository.existsByGameAndRoundNumber(game, currentRound.getRoundNumber() + 1)) {
            throw new ApiException(ExceptionEnum.DUPLICATE_ROUND);
        }

        int newRoundNumber = currentRound.getRoundNumber() + 1; // 기존 라운드 + 1

        // 유효하지 않은 라운드 번호 체크
        if (newRoundNumber <= 0) {
            throw new ApiException(ExceptionEnum.INVALID_ROUND_NUMBER);
        }

        // 다음 라운드 번호가 이미 존재하는지 확인
        if (roundRepository.existsByGameAndRoundNumber(game, newRoundNumber)) {
            throw new ApiException(ExceptionEnum.DUPLICATE_ROUND);
        }

        // 새로운 라운드 생성
        Round newRound = new Round();
        newRound.setGame(game);
        newRound.setRoundNumber(newRoundNumber);
        int durationMinutes = currentRound.getDurationMinutes();
        newRound.setDurationMinutes(durationMinutes);
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
        // 가장 최근의 라운드를 조회함
        Round latestRound = roundRepository.findFirstByGameOrderByRoundNumberDesc(game)
                .orElseThrow(() -> new ApiException(ExceptionEnum.ROUND_NOT_FOUND));

        // 현재 라운드 번호가 종료 라운드 번호 이상인지 확인여부
        return latestRound.getRoundNumber() >= endRoundNumber;
    }
}
