package com.kmbbj.backend.games.service.round;

import com.kmbbj.backend.games.dto.CurrentRoundDTO;
import com.kmbbj.backend.games.dto.RoundRankingSimpleDTO;
import com.kmbbj.backend.games.dto.RoundResultDTO;
import com.kmbbj.backend.games.entity.Game;
import com.kmbbj.backend.games.entity.GameBalance;
import com.kmbbj.backend.games.entity.Round;
import com.kmbbj.backend.games.entity.RoundRanking;
import com.kmbbj.backend.games.enums.GameStatus;
import com.kmbbj.backend.games.repository.*;
import com.kmbbj.backend.games.service.gameresult.GameResultService;
import com.kmbbj.backend.games.service.roundresult.RoundResultService;
import com.kmbbj.backend.games.util.GameEncryptionUtil;
import com.kmbbj.backend.games.util.GameProperties;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import com.kmbbj.backend.matching.entity.Room;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RoundServiceImpl implements RoundService {


    private final RoundRepository roundRepository;
    private final GameRepository gameRepository;
    private final GameProperties gameProperties;
    private final GameEncryptionUtil gameEncryptionUtil;
    private final RoundResultService roundResultService;
    private final GameResultService gameResultService;
    private final GameBalanceRepository gameBalanceRepository;
    private final RoundRankingRepository roundRankingRepository;


    /** 암호화된 게임 ID 가져와서 라운드 관리
     *
     * 현재 라운드 확인 후 게임 종료 확인 여부
     * 게임이 종료 하면 결과 처리
     * 게임 계속 진행 중이면 현재 라운드 처리하고 다음 라운드 진행
     *
     * @param gameId 암호화된 ID
     * @return 게임 종료 되면 True 아니면 False
     */
    @Override
    @Transactional
    public boolean manageRounds(UUID gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ApiException(ExceptionEnum.GAME_NOT_FOUND));

        Round currentRound = getCurrentRound(game);
        Room room = game.getRoom();

        if (currentRound.getRoundNumber() >= room.getEnd()) {
            endGame(game, gameId);
            return true; // 게임 종료
        } else {
            processCurrentRoundAndStartNext(game, currentRound);
            return false; // 게임 계속 진행
        }
    }

    /** 현재 라운드의 결과를 처리하고 다음 라운드를 시작하는 메서드
     *
     * 현재 라운드 결과 처리 한 후 중간 라운드인 경우 중간 순위 계산
     * 그 후 새로운 라운드 시작
     *
     * @param game 현재 게임 객체
     * @param currentRound 현재 라운드 객체
     */
    private void processCurrentRoundAndStartNext(Game game, Round currentRound) {
        processRoundResult(currentRound); // 현재 라운드 결과 처리
        RoundResultDTO roundResultDTO = roundResultService.calculateRoundResult(game.getGameId(),currentRound.getRoundId());

        roundResultService.saveRoundResult(roundResultDTO);
        // 현재 라운드가 중간 라운드인지 확인 후 중간 순위 계산
        if (isMiddleRound(currentRound)) {
            calculateMidGameRanking(game);
        }
        startNewRound(game); // 새로운 라운드 시작
    }


    /** 라운드의 결과를 처리하는 메서드
     * 각 플레이어 시드머니 변화 계산
     * 이를 기반으로 순위 매김
     *
     * @param round 처리할 라운드 객체
     */
    private void processRoundResult(Round round) {
        Game game = round.getGame(); // 라운드 해당하는 게임 정보 가져옴

        // 해당 게임의 잔액 정보 가져옴
        List<GameBalance> currentBalances = gameBalanceRepository.findByGame(game);
        List<RoundRanking> rankings = new ArrayList<>();

        long initialSeedMoney = game.getRoom().startSeedMoneyLong();

        for (GameBalance balance : currentBalances) {
            RoundRanking ranking = new RoundRanking();
            ranking.setRound(round);
            ranking.setUser(balance.getUser());

            long currentMoney = balance.getSeed();
            long difference = currentMoney - initialSeedMoney;

            if (difference > 0) {
                ranking.setProfit(String.valueOf(difference));
                ranking.setLoss("0");
            } else if (difference < 0) {
                ranking.setProfit("0");
                ranking.setLoss(String.valueOf(Math.abs(difference)));
            } else {
                ranking.setProfit("0");
                ranking.setLoss("0");
            }

            rankings.add(ranking);
        }

        // 순수익(profit - loss)을 기준으로 내림차순 정렬
        rankings.sort((a, b) -> Long.compare(
                Long.parseLong(b.getProfit()) - Long.parseLong(b.getLoss()),
                Long.parseLong(a.getProfit()) - Long.parseLong(a.getLoss())
        ));

        // 순위 할당
        for (int i = 0; i < rankings.size(); i++) {
            rankings.get(i).setRank(i + 1);
        }

        roundRankingRepository.saveAll(rankings);
    }

    /** 중간 순위 각 라운드 별 순위 반환
     *
     * @param encryptedGameId 암호화된 게임 ID
     * @return 중간 순위 라운드별 순위가 포함된 MidGameRankingResponse 객체
     */


    /** 새로운 라운드를 시작하는 메서드
     * 현재 라운드 번호에서 +1을 하여 새로운 라운드 생성한 후 저장
     * 게임 상태 ACTIVE 설정하고 계속 진행
     *
     * @param game 새 라운드를 시작할 Game 객체
     */
    @Override
    @Transactional
    public CurrentRoundDTO startNewRound(Game game) {
        Round currentRound = getCurrentRound(game); // 현재 라운드 정보 가져옴
        int newRoundNumber = currentRound.getRoundNumber();// 라운드 + 1 추가

        // 새로운 라운드 객체 생성 및 설정
        Round newRound = new Round();
        newRound.setGame(game); // 게임
        newRound.setRoundNumber(newRoundNumber); // 새로운 라운드 번호
        newRound.setDurationMinutes(getDurationMinutes()); // 라운드 지속 시간

        roundRepository.save(newRound); // 새로운 라운드 DB 저장

        game.setGameStatus(GameStatus.ACTIVE); // 게임 상태 ACTIVE 저장
        gameRepository.save(game);

        // 새로운 라운드 정보 반환을 위한 DTO 생성
        CurrentRoundDTO currentRoundDTO = new CurrentRoundDTO();
        currentRoundDTO.setRoundNumber(newRound.getRoundNumber());
        currentRoundDTO.setDurationMinutes(newRound.getDurationMinutes());
        currentRoundDTO.setGameStatus(game.getGameStatus().toString());

        return currentRoundDTO;
    }
    @Override
    @Transactional
    public CurrentRoundDTO endCurrentAndStartNextRound(UUID gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ApiException(ExceptionEnum.GAME_NOT_FOUND));
        Round currentRound = roundRepository.findFirstByGameOrderByRoundNumberDesc(game)
                .orElseThrow(() -> new ApiException(ExceptionEnum.ROUND_NOT_FOUND));
        Room room = game.getRoom();

        boolean isLastRound = currentRound.getRoundNumber() >= room.getEnd();

        if (isLastRound) {
            game.setGameStatus(GameStatus.COMPLETED); // 게임 상태 COMPLETED
            gameRepository.save(game);

            // 게임 결과 생성
            gameResultService.createGameResults(gameId);
            throw new ApiException(ExceptionEnum.GAME_ALREADY_ENDED); // 게임이 이미 종료된 경우 예외 발생
        } else {
            processRoundResult(currentRound); // 현재 라운드 결과 처리
            RoundResultDTO roundResultDTO = roundResultService.calculateRoundResult(game.getGameId(),currentRound.getRoundId());

            roundResultService.saveRoundResult(roundResultDTO);
            // 현재 라운드가 중간 라운드인지 확인 후 중간 순위 계산
            if (isMiddleRound(currentRound)) {
                List<GameBalance> currentBalances = gameBalanceRepository.findByGame(game);
                List<RoundRanking> midGameRankings = new ArrayList<>();
                long initialSeedMoney = game.getRoom().startSeedMoneyLong();

                for (GameBalance balance : currentBalances) {
                    RoundRanking midRanking = new RoundRanking();
                    midRanking.setUser(balance.getUser());
                    midRanking.setRound(currentRound);

                    long currentMoney = balance.getSeed();
                    long difference = currentMoney - initialSeedMoney;

                    if (difference >= 0) {
                        midRanking.setProfit(String.valueOf(difference));
                        midRanking.setLoss("0");
                    } else {
                        midRanking.setProfit("0");
                        midRanking.setLoss(String.valueOf(Math.abs(difference)));
                    }

                    midGameRankings.add(midRanking);
                }

                // 게임머니(현재 잔액)를 기준으로 내림차순 정렬
                midGameRankings.sort((a, b) -> Long.compare(
                        Long.parseLong(b.getProfit()) - Long.parseLong(b.getLoss()),
                        Long.parseLong(a.getProfit()) - Long.parseLong(a.getLoss())
                ));

                // 순위 할당
                for (int i = 0; i < midGameRankings.size(); i++) {
                    midGameRankings.get(i).setRank(i + 1);
                }

                roundRankingRepository.saveAll(midGameRankings);
            }

            // 새로운 라운드 객체 생성 및 설정
            Round newRound = new Round();
            int newRoundNumber = currentRound.getRoundNumber() + 1;
            newRound.setGame(game); // 게임
            newRound.setRoundNumber(newRoundNumber); // 새로운 라운드 번호
            newRound.setDurationMinutes(getDurationMinutes()); // 라운드 지속 시간

            roundRepository.save(newRound); // 새로운 라운드 DB 저장
            currentRound = newRound;
        }

        // 새로운 라운드 정보를 반환합니다.
        CurrentRoundDTO currentRoundDTO = new CurrentRoundDTO();
        currentRoundDTO.setRoundNumber(currentRound.getRoundNumber());
        currentRoundDTO.setTotalRounds(game.getRoom().getEnd());
        currentRoundDTO.setDurationMinutes(currentRound.getDurationMinutes());
        currentRoundDTO.setGameStatus(game.getGameStatus().toString());

        return currentRoundDTO;
    }

    /** 게임 종료
     *
     * @param game 종료할 게임 객체
     * @param gameId 암호화된 ID
     */
    private void endGame(Game game, UUID gameId) {
        game.setGameStatus(GameStatus.COMPLETED); // 게임 상태 COMPLETED
        gameRepository.save(game);

        // 게임 결과 생성
        gameResultService.createGameResults(gameId);
    }


    /**
     * 게임의 현재 라운드를 조회하는 메서드.
     *
     * @param game 라운드를 조회할 Game 객체
     * @return 가장 최근의 Round 객체
     * @throws ApiException 라운드를 찾을 수 없는 경우
     */
    private Round getCurrentRound(Game game) {
        return roundRepository.findFirstByGameOrderByRoundNumberDesc(game)
                .orElseThrow(() -> new ApiException(ExceptionEnum.ROUND_NOT_FOUND));
    }


    /**
     * 현재 라운드가 중간 라운드인지 확인하는 메서드
     *
     * @param round 확인할 라운드 객체
     * @return 중간 라운드이면 true, 아니면 false
     */
    private boolean isMiddleRound(Round round) {
        return round.getRoundNumber() == round.getGame().getRoom().getEnd() / 2;
    }


    /** 중간 순위 계산
     * 현재 라운드의 게임머니를 기준으로 이익/손실 및 순위 계산
     *
     * @param game 현재 게임 객체
     */
    private void calculateMidGameRanking(Game game) {
        List<GameBalance> currentBalances = gameBalanceRepository.findByGame(game);
        List<RoundRanking> midGameRankings = new ArrayList<>();
        Round currentRound = getCurrentRound(game);
        long initialSeedMoney = game.getRoom().startSeedMoneyLong();

        for (GameBalance balance : currentBalances) {
            RoundRanking midRanking = new RoundRanking();
            midRanking.setUser(balance.getUser());
            midRanking.setRound(currentRound);

            long currentMoney = balance.getSeed();
            long difference = currentMoney - initialSeedMoney;

            if (difference >= 0) {
                midRanking.setProfit(String.valueOf(difference));
                midRanking.setLoss("0");
            } else {
                midRanking.setProfit("0");
                midRanking.setLoss(String.valueOf(Math.abs(difference)));
            }

            midGameRankings.add(midRanking);
        }

        // 게임머니(현재 잔액)를 기준으로 내림차순 정렬
        midGameRankings.sort((a, b) -> Long.compare(
                Long.parseLong(b.getProfit()) - Long.parseLong(b.getLoss()),
                Long.parseLong(a.getProfit()) - Long.parseLong(a.getLoss())
        ));

        // 순위 할당
        for (int i = 0; i < midGameRankings.size(); i++) {
            midGameRankings.get(i).setRank(i + 1);
        }

        roundRankingRepository.saveAll(midGameRankings);
    }


    @Override
    public List<List<RoundRankingSimpleDTO>> getRoundRankingsForGame(UUID gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ApiException(ExceptionEnum.GAME_NOT_FOUND));

        // 게임에 속한 모든 라운드 조회
        List<Round> rounds = roundRepository.findByGameOrderByRoundNumberAsc(game);
        List<List<RoundRankingSimpleDTO>> allRoundRankings = new ArrayList<>();

        // 각 라운드에 대한 순위를 DTO로 변환하여 저장
        for (Round round : rounds) {
            List<RoundRanking> roundRankings = roundRankingRepository.findByRoundOrderByRankAsc(round);
            List<RoundRankingSimpleDTO> roundRankingDTOs = new ArrayList<>();

            // RoundRanking을 RoundRankingSimpleDTO로 변환
            for (RoundRanking ranking : roundRankings) {
                RoundRankingSimpleDTO dto = new RoundRankingSimpleDTO(
                        ranking.getUser().getNickname(), // 닉네임
                        ranking.getRank(),              // 순위
                        ranking.getProfit(),            // 이익
                        ranking.getLoss()               // 손익
                );
                roundRankingDTOs.add(dto); // 변환된 DTO 리스트 추가
            }

            allRoundRankings.add(roundRankingDTOs);
        }

        return allRoundRankings;
    }

    @Override
    public List<RoundRankingSimpleDTO> getCurrentRoundRankingsForGame(UUID gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ApiException(ExceptionEnum.GAME_NOT_FOUND));

        // 게임에 속한 모든 라운드 조회
        List<Round> rounds = roundRepository.findByGameOrderByRoundNumberAsc(game);
        Map<Long, RoundRankingSimpleDTO> roundRankingMap = new HashMap<>();


        // 각 라운드에 대한 순위를 DTO로 변환하여 저장
        for (Round round : rounds) {
            List<RoundRanking> roundRankings = roundRankingRepository.findByRoundOrderByRankAsc(round);

            // RoundRanking을 RoundRankingSimpleDTO로 변환
            for (RoundRanking ranking : roundRankings) {
                RoundRankingSimpleDTO previousRoundRanking = roundRankingMap.get(ranking.getUser().getId());
                if (previousRoundRanking == null) {
                    roundRankingMap.put(ranking.getUser().getId(),
                            new RoundRankingSimpleDTO(
                                    ranking.getUser().getNickname(), // 닉네임
                                    0,                              // 순위
                                    ranking.getProfit(),            // 이익
                                    ranking.getLoss()               // 손익
                            ));
                } else {
                    Long totalProfit = Long.parseLong(ranking.getProfit());
                    Long totalLoss = Long.parseLong(ranking.getLoss());

                    Long currentProfit = Long.parseLong(previousRoundRanking.getProfit());
                    Long currentLoss = Long.parseLong(previousRoundRanking.getLoss());

                    previousRoundRanking.setProfit(String.valueOf(totalProfit + currentProfit));
                    previousRoundRanking.setLoss(String.valueOf(totalLoss + currentLoss));

                    roundRankingMap.put(ranking.getUser().getId(), previousRoundRanking);
                }
                RoundRankingSimpleDTO dto = new RoundRankingSimpleDTO(
                        ranking.getUser().getNickname(), // 닉네임
                        ranking.getRank(),              // 순위
                        ranking.getProfit(),            // 이익
                        ranking.getLoss()               // 손익
                );
            }
        }

        List<RoundRankingSimpleDTO> currentRoundRanking = roundRankingMap.values().stream().toList();
        currentRoundRanking.sort((a, b) -> Long.compare(
                Long.parseLong(b.getProfit()) - Long.parseLong(b.getLoss()),
                Long.parseLong(a.getProfit()) - Long.parseLong(a.getLoss())
        ));

        return currentRoundRanking;
    }


    // 게임 라운드의 지속시간 (24시간)
    private int getDurationMinutes() {
        return gameProperties.getGameRoundDurationMinutes();
    }
}
