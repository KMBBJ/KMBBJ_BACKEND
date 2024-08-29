package com.kmbbj.backend.games.service.gameresult;
import com.kmbbj.backend.auth.entity.User;
import com.kmbbj.backend.balance.entity.AssetTransaction;
import com.kmbbj.backend.balance.entity.ChangeType;
import com.kmbbj.backend.balance.entity.TotalBalance;
import com.kmbbj.backend.balance.repository.totalbalances.TotalBalancesRepository;
import com.kmbbj.backend.balance.repository.transaction.AssetTransactionRepository;
import com.kmbbj.backend.games.entity.Game;
import com.kmbbj.backend.games.entity.GameBalance;
import com.kmbbj.backend.games.entity.GameResult;
import com.kmbbj.backend.games.repository.GameBalanceRepository;
import com.kmbbj.backend.games.repository.GameRepository;
import com.kmbbj.backend.games.repository.GameResultRepository;
import com.kmbbj.backend.games.util.GameEncryptionUtil;

import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import com.kmbbj.backend.matching.entity.Room;
import com.kmbbj.backend.matching.entity.UserRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameResultServiceImpl implements GameResultService{

    private final GameRepository gameRepository;
    private final GameBalanceRepository gameBalanceRepository;
    private final GameResultRepository gameResultRepository;
    private final GameEncryptionUtil gameEncryptionUtil;
    private final TotalBalancesRepository totalBalancesRepository;
    private final AssetTransactionRepository assetTransactionRepository;


    /** 게임 종료 시 모든 참여자의 게임 결과를 생성
     *
     * @param encryptedGameId 암호화된 게임 ID
     * @return 생성된 GameResult 객체 리스트
     */
    @Override
    public List<GameResult> createGameResults(String encryptedGameId) {
        // 암호화된 ID -> 게임 ID를 복호화
        UUID gameId = gameEncryptionUtil.decryptToUUID(encryptedGameId);
        // 게임 정보 조회 없으면 예외 발생
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ApiException(ExceptionEnum.GAME_NOT_FOUND));
        // 게임이 진행될 방 정보 가져옴
        Room room = game.getRoom();

        // 방에 참여한 사용자 중 실제로 게임 플레이한 사용자만 필터링
        List<User> participants = room.getUserRooms().stream()
                .filter(UserRoom::getIsPlayed) // 실제로 플레이한 사용자
                .map(UserRoom::getUser) // UserRoom -> User 객체 수출
                .collect(Collectors.toList());

        // 게임에 참여한 사용자들의 게임 잔액 정보 조회
        List<GameBalance> gameBalances = gameBalanceRepository.findByGameAndUserIn(game, participants);

        // 게임 잔액을 기준으로 내림차순 정렬하여 순위 결정
        List<GameBalance> sortedBalances = gameBalances.stream()
                .sorted((b1, b2) -> b2.getSeed().compareTo(b1.getSeed()))
                .collect(Collectors.toList());

        List<GameResult> results = new ArrayList<>();

        for (int i = 0; i < sortedBalances.size(); i++) {
            GameBalance balance = sortedBalances.get(i);
            int rank = i + 1; // 순위 1부터 시작
            GameResult result = createGameResult(game, balance.getUser(), balance.getSeed(), i + 1);
            results.add(result);

            // 순위에 따른 고정 보상 계산
            Long reward = calculateFixedReward(rank);

            // 사용자 보유 자산 업데이트 (보상 지급)
            updateUserTotalBalance(balance.getUser().getId(), reward);
        }


        return results;
    }

    /** 개별 사용자의 게임 결과를 생성하는 메서드
     *
     * @param game 게임 객체
     * @param user 사용자 객체
     * @param finalBalance 사용자의 최종 잔액
     * @param rank 사용자 순위
     * @return 저장된 GameResult 반환
     */
    private GameResult createGameResult(Game game, User user, Long finalBalance, int rank) {
        // 게임 시작할 때의 초기 시드머니를 가져옴
        Long initialSeed = Long.valueOf(game.getRoom().getStartSeedMoney());

        // 사용자 최종 잔액과 초기 시드 비교하여 총 수익 ,총 손실 계산
        long totalProfit = finalBalance > initialSeed ? finalBalance - initialSeed : 0;
        long totalLoss = finalBalance < initialSeed ? initialSeed - finalBalance : 0;

        //GameResult 객체를 생성한 후 게임, 사용자 ID , 수익, 손실, 순위 정보 설정
        GameResult gameResult = new GameResult();
        gameResult.setGame(game);
        gameResult.setUserId(user.getId());
        gameResult.setTotalProfit(totalProfit);
        gameResult.setTotalLoss(totalLoss);
        gameResult.setUserRank(rank);

        // 생성된 GameResult 객체를 저장하고 반환
        return gameResultRepository.save(gameResult);
    }

    /** 사용자 자산 업데이트 (순위에 따른 보상 지급)
     *
     * @param userId 사용자 ID
     * @param reward 순위에 따른 보상 금액
     */
    private void updateUserTotalBalance(Long userId, Long reward) {
        // 사용자 자산 정보 조회
        TotalBalance totalBalance = totalBalancesRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(ExceptionEnum.TOTAL_BALANCE_NOT_FOUND));

        AssetTransaction transaction = AssetTransaction.builder()
                .changeType(ChangeType.GAME)
                .changeAmount(reward)
                .totalBalance(totalBalance)
                .build();

        // 거래 내역 저장
        assetTransactionRepository.save(transaction);
        // 사용자 총 자산 업데이트
        totalBalance.changeAsset(reward);
        // 변경된 자산 정보 저장
        totalBalancesRepository.save(totalBalance);
    }


    /** 순위에 따른 고정 보상 계산
     *
     * @param rank 사용자 순위
     * @return 계산된 보상 금액
     */
    private Long calculateFixedReward(int rank) {
        // 순위별 고정 보상 금액
        if (rank == 1) {
            return 5000000L;  // 1위는 500만 원
        } else if (rank == 2) {
            return 3000000L;  // 2위는 300만 원
        } else if (rank == 3) {
            return 1000000L;  // 3위는 100만 원
        } else {
            return 0L;  // 꼴등 ...?
        }
    }
}
