package com.kmbbj.backend.games.service.gamebalance;
import com.kmbbj.backend.auth.entity.User;


import com.kmbbj.backend.feature.exchange.controller.response.TransactionsResponse;
import com.kmbbj.backend.feature.exchange.entity.TransactionType;
import com.kmbbj.backend.feature.exchange.repository.postgre.TransactionRepository;
import com.kmbbj.backend.games.dto.GameBalanceDTO;
import com.kmbbj.backend.games.entity.Game;
import com.kmbbj.backend.games.entity.GameBalance;
import com.kmbbj.backend.games.repository.CoinBalanceRepository;
import com.kmbbj.backend.games.repository.GameBalanceRepository;

import com.kmbbj.backend.games.util.GameEncryptionUtil;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import com.kmbbj.backend.matching.entity.UserRoom;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameBalanceServiceImpl implements GameBalanceService {

    // 게임 계좌 레퍼지토리
    private final GameBalanceRepository gameBalanceRepository;
    private final TransactionRepository transactionRepository;

    /**
     * 게임 시작 시 호출 , 방에 설정된 시드머니 초기 잔액 사용
     *
     * @param game 잔액을 생성할 게임 객체
     * @return 생성된 GameBalance 객체
     */
    @Override
    @Transactional
    public List<GameBalance> createGameBalance(Game game) {
        // 방의 시작 시드머니 가져옴
        Long seedMoney = Long.valueOf((String.valueOf(game.getRoom().getStartSeedMoney().getAmount())));

        // 실제로 게임을 플레이한 사용자 리스트 가져오기
        List<User> participants = game.getRoom().getUserRooms().stream()
                .filter(UserRoom::getIsPlayed) // 실제로 플레이한 사용자 필터링
                .map(UserRoom::getUser) // UserRoom -> User 객체 추출
                .collect(Collectors.toList());

        // 각 사용자에 대한 게임 잔액 생성
        List<GameBalance> gameBalances = new ArrayList<>();

        // 각 사용자에 대한 게임 잔액 생성
        for (User user : participants) {
            GameBalance gameBalance = new GameBalance();
            gameBalance.setGame(game);  // 게임 정보 설정
            gameBalance.setUser(user);  // 사용자 정보 설정
            gameBalance.setSeed(seedMoney); // 시작 자본 설정

            // 생성된 GameBalance 리스트에 추가
            gameBalances.add(gameBalance);
        }

        // GameBalance 객체를 데이터베이스 저장
        gameBalanceRepository.saveAll(gameBalances);

        return gameBalances;
    }

    /** 사용자 계좌 변동 내역
     *
     * @param userId 유저 ID
     * @return 사용자 계좌 변동
     */
    @Override
    @Transactional
    public GameBalanceDTO getGameBalance(Long userId) {
        // UserID에 GameBalance 조회
        GameBalance gameBalance = gameBalanceRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(ExceptionEnum.BALANCE_NOT_FOUND));

        // 조회된 초기 자본 가져오고
        Long initialBalance = Long.valueOf(String.valueOf(gameBalance.getGame().getRoom().getStartSeedMoney().getAmount()));

        // 해당 TransactionsResponse 의 ID 가져와서 모든 거래 내역 조회
        List<TransactionsResponse> transactions = transactionRepository.findAllByBalancesIdWithCoinSymbol(gameBalance.getGameBalancesId());


        Long currentBalance = calculateCurrentBalance(initialBalance, transactions); // 초기 자본과 거래 통해서 현재 잔액 계산
        Long orderAmount = calculateTotalOrderAmount(transactions); // 거래 내역 통해서 총 주문 금액 계산
        Long profitAmount = Math.max(currentBalance - initialBalance, 0L); // 초기 자본 대비 현재 잔액 차이 (이익)
        Long lossAmount = Math.max(initialBalance - currentBalance, 0L); //// 초기 자본 대비 현재 잔액 차이 (손실)

        // 마지막 거래 심볼 가져옴 없으면  "" 처리
        String lastSymbol = transactions.isEmpty() ? "" : transactions.get(transactions.size() - 1).getSymbol();
        // 마지막 거래 가격 가져옴 없으면 0
        Long lastPrice = transactions.isEmpty() ? 0L : transactions.get(transactions.size() - 1).getPrice();

        GameBalanceDTO gameBalanceDTO = new GameBalanceDTO();
        gameBalanceDTO.setInitialBalance(initialBalance); // 초기 자본
        gameBalanceDTO.setCurrentBalance(currentBalance); // 현재 잔액
        gameBalanceDTO.setOrderAmount(orderAmount); // 총 주문 금액
        gameBalanceDTO.setProfitAmount(profitAmount); // 이익 금액
        gameBalanceDTO.setLossAmount(lossAmount); // 손실 금액
        gameBalanceDTO.setSymbol(lastSymbol); // 마지막 거래 코인 이름
        gameBalanceDTO.setPrice(lastPrice); // 마지막 거래의 가격 설정

        return gameBalanceDTO;
    }

    /**
     * 게임 종료 시 호출, 시드 전부 삭제
     *
     * @param game 삭제할 game balance 의 game 정보
     * @return 삭제된 GameBalance 목록
     */
    @Override
    @Transactional
    public List<GameBalance> deleteGameBalances(Game game) {
        List<GameBalance> gameBalanceList = gameBalanceRepository.findByGame(game);
        gameBalanceRepository.deleteAllById(gameBalanceList.stream().map(GameBalance::getGameBalancesId).toList());
        return gameBalanceList;
    }

    private Long calculateCurrentBalance(Long initialBalance, List<TransactionsResponse> transactions) {
        Long balance = initialBalance; // 초기 자본 -> 현재 잔액 설정

        // 모든 거래 내역 반복
        for (TransactionsResponse transaction : transactions) {
            if (transaction.getTransactionType() == TransactionType.BUY) { // 구매인 경우
                balance -= transaction.getTotalPrice(); // 구매 금액만큼 차감
            } else if (transaction.getTransactionType() == TransactionType.SELL) { // 판매인 경우
                balance += transaction.getTotalPrice(); // 판매 금액만큼 추가
            }
        }
        return balance;
    }

    private Long calculateTotalOrderAmount(List<TransactionsResponse> transactions) {
        return transactions.stream()
                .mapToLong(TransactionsResponse::getTotalPrice)
                .sum();
    }


}

