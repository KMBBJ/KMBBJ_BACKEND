package com.kmbbj.backend.games.service.roundresult;

import com.kmbbj.backend.charts.entity.coin.Coin;
import com.kmbbj.backend.charts.repository.coin.CoinRepository;
import com.kmbbj.backend.feature.exchange.entity.TransactionType;
import com.kmbbj.backend.feature.exchange.entity.postgre.Transaction;
import com.kmbbj.backend.feature.exchange.repository.postgre.TransactionRepository;
import com.kmbbj.backend.games.dto.RoundResultDTO;
import com.kmbbj.backend.games.entity.Game;
import com.kmbbj.backend.games.entity.Round;
import com.kmbbj.backend.games.entity.RoundResult;
import com.kmbbj.backend.games.repository.GameRepository;
import com.kmbbj.backend.games.repository.RoundRepository;
import com.kmbbj.backend.games.repository.RoundResultRepository;
import com.kmbbj.backend.games.util.GameEncryptionUtil;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoundResultServiceImpl implements RoundResultService {

    private final TransactionRepository transactionRepository;
    private final CoinRepository coinRepository;
    private final RoundResultRepository roundResultRepository;
    private final RoundRepository roundRepository;
    private final GameRepository gameRepository;
    private final GameEncryptionUtil gameEncryptionUtil;


    /**
     * 게임 ID , 라운드 ID에 대한 라운드 결과 계산 하고 반환
     * 모든 거래 내역 가져와서
     * 각 코인의 매수량, 수익, 손실 계산하고, 가장 큰 값을 기준으로 라운드 결과 구성
     *
     * @param gameId  게임 ID
     * @param roundId 라운드 ID
     * @return RoundResultDTO 라운드 결과 DTO
     */
    @Override
    public RoundResultDTO calculateRoundResult(UUID gameId, Long roundId) {
        // 게임 ID로 거래 내역 조회함
        List<Transaction> transactions = transactionRepository.findByGameId(gameId);


        Map<Long, BigDecimal> coinBuyQuantityMap = new HashMap<>(); // 각 코인별 매수량 저장
        Map<Long, BigDecimal> coinProfitMap = new HashMap<>(); // 각 코인별 수익 저장
        Map<Long, BigDecimal> coinLossMap = new HashMap<>(); // 각 코인별 손실 저장

        // 거래 내역을 순회하면서 매수,매도 거래 구분하여 집계함
        for (Transaction transaction : transactions) {
            if (transaction.getTransactionType() == TransactionType.BUY) {
                // 매수 거래인 경우 coinBuyQuantityMap 추가함
                coinBuyQuantityMap.merge(transaction.getCoinId(), transaction.getQuantity(), BigDecimal::add);
            } else if (transaction.getTransactionType() == TransactionType.SELL) {
                // 매도 거래인 경우, 수익 or 손실 계산
                BigDecimal profitOrLoss = calculateProfitOrLoss(transaction);
                if (profitOrLoss.compareTo(BigDecimal.ZERO) > 0) {
                    // 수익이 발생하면 coinProfitMap 에 수익 추가
                    coinProfitMap.merge(transaction.getCoinId(), profitOrLoss, BigDecimal::add);
                } else {
                    // 손실 발생한 경우, coinLossMap 손실을 추가
                    coinLossMap.merge(transaction.getCoinId(), profitOrLoss.abs(), BigDecimal::add);
                }
            }
        }

        // 라운드 결과 DTO를 생성
        RoundResultDTO resultDTO = new RoundResultDTO();
        resultDTO.setRoundId(roundId);

        // 가장 많이 매수된 코인을 계산하고 DTO에 설정
        Long topBuyCoinId = findTopCoinByQuantity(coinBuyQuantityMap);
        resultDTO.setTopBuyCoin(getCoinNameById(topBuyCoinId));  // 코인 이름
        resultDTO.setTopBuyPercent(calculatePercent(coinBuyQuantityMap, topBuyCoinId));

        // 가장 많이 수익을 낸 코인을 계산하고 DTO에 설정
        Long topProfitCoinId = findTopCoinByProfit(coinProfitMap);
        resultDTO.setTopProfitCoin(getCoinNameById(topProfitCoinId));  // 코인 이름
        resultDTO.setTopProfitPercent(calculatePercent(coinProfitMap, topProfitCoinId));

        // 가장 많이 손실을 본 코인을 계산하고 DTO에 설정
        Long topLossCoinId = findTopCoinByLoss(coinLossMap);
        resultDTO.setTopLossCoin(getCoinNameById(topLossCoinId));  // 코인 이름
        resultDTO.setTopLossPercent(calculatePercent(coinLossMap, topLossCoinId));

        //라운드 결과 DTO 반환
        return resultDTO;
    }

    @Override
    public void saveRoundResult(RoundResultDTO roundResultDTO) {
        // DTO에서 라운드 ID로 Round 엔티티를 조회
        Round round = roundRepository.findById(roundResultDTO.getRoundId())
                .orElseThrow(() -> new ApiException(ExceptionEnum.ROUND_NOT_FOUND));

        RoundResult roundResult = new RoundResult();
        roundResult.setRound(round);
        roundResult.setTopBuyCoin(roundResultDTO.getTopBuyCoin());
        roundResult.setTopBuyPercent(roundResultDTO.getTopBuyPercent());
        roundResult.setTopProfitCoin(roundResultDTO.getTopProfitCoin());
        roundResult.setTopProfitPercent(roundResultDTO.getTopProfitPercent());
        roundResult.setTopLossCoin(roundResultDTO.getTopLossCoin());
        roundResult.setTopLossPercent(roundResultDTO.getTopLossPercent());

        // DB 저장
        roundResultRepository.save(roundResult);
    }

    @Override
    public List<RoundResultDTO> getCompletedRoundResultsForGame(String encryptedGameId) {
        Game game = getGameByEncryptedId(encryptedGameId);
        List<Round> allRounds = roundRepository.findByGameOrderByRoundNumberAsc(game);

        return allRounds.stream()
                .map(round -> calculateRoundResult(game.getGameId(), round.getRoundId()))
                .collect(Collectors.toList());
    }

    /**
     * 매도 거래에서 발생한 수익 또는 손실을 계산합니다.
     * 수익 = (거래된 총 가격 - 거래된 가격)
     *
     * @param transaction 매도 거래 정보
     * @return 수익 또는 손실 (BigDecimal)
     */
    private BigDecimal calculateProfitOrLoss(Transaction transaction) {
        return BigDecimal.valueOf(transaction.getTotalPrice() - transaction.getPrice());
    }

    /**
     * 주어진 코인의 매수량 정보를 기반으로 가장 많이 매수된 코인의 ID를 찾습니다.
     *
     * @param coinQuantityMap 코인별 매수량 정보 (Map<Long, BigDecimal>)
     * @return 가장 많이 매수된 코인의 ID (Long)
     */
    private Long findTopCoinByQuantity(Map<Long, BigDecimal> coinQuantityMap) {
        return coinQuantityMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())  // 매수량이 가장 큰 코인을 찾음
                .orElseThrow(() -> new ApiException(ExceptionEnum.COIN_NOT_FOUND))  // 코인이 없을 경우 예외 발생
                .getKey();
    }

    /**
     * 주어진 코인의 수익 정보를 기반으로 가장 많은 수익을 낸 코인의 ID를 찾습니다.
     *
     * @param coinProfitMap 코인별 수익 정보 (Map<Long, BigDecimal>)
     * @return 가장 많은 수익을 낸 코인의 ID (Long)
     */
    private Long findTopCoinByProfit(Map<Long, BigDecimal> coinProfitMap) {
        return coinProfitMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())  // 수익이 가장 큰 코인을 찾음
                .orElseThrow(() -> new ApiException(ExceptionEnum.COIN_NOT_FOUND))  // 코인이 없을 경우 예외 발생
                .getKey();
    }

    /**
     * 주어진 코인의 손실 정보를 기반으로 가장 많은 손실을 본 코인의 ID를 찾습니다.
     *
     * @param coinLossMap 코인별 손실 정보 (Map<Long, BigDecimal>)
     * @return 가장 많은 손실을 본 코인의 ID (Long)
     */
    private Long findTopCoinByLoss(Map<Long, BigDecimal> coinLossMap) {
        return coinLossMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())  // 손실이 가장 큰 코인을 찾음
                .orElseThrow(() -> new ApiException(ExceptionEnum.COIN_NOT_FOUND))  // 코인이 없을 경우 예외 발생
                .getKey();
    }

    /**
     * 주어진 코인 ID에 해당하는 코인의 매수량 또는 수익 비율을 계산합니다.
     *
     * @param map 코인별 매수량 또는 수익 정보 (Map<Long, BigDecimal>)
     * @param coinId 계산할 코인의 ID (Long)
     * @return 해당 코인의 비율 (%)
     */
    private String calculatePercent(Map<Long, BigDecimal> map, Long coinId) {
        // 모든 코인의 합계를 구한 후 특정 코인의 비율을 계산하고 반환
        BigDecimal total = map.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal percent = map.get(coinId).multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP);
        return percent.toString();
    }

    /**
     * 코인 ID를 기반으로 해당 코인의 이름을 조회합니다.
     *
     * @param coinId 코인 ID (Long)
     * @return 코인 이름 (String)
     */
    private String getCoinNameById(Long coinId) {
        return coinRepository.findById(coinId)
                .map(Coin::getCoinName)
                .orElseThrow(() -> new ApiException(ExceptionEnum.COIN_NOT_FOUND));
    }

    // 암호화된 게임 ID 게임 객체 조회
    private Game getGameByEncryptedId(String encryptedGameId) {
        UUID gameId = gameEncryptionUtil.decryptToUUID(encryptedGameId);
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new ApiException(ExceptionEnum.GAME_NOT_FOUND));
    }

}
