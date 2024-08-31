package com.kmbbj.backend.admin.service;

import com.kmbbj.backend.auth.entity.User;
import com.kmbbj.backend.auth.repository.UserRepository;
import com.kmbbj.backend.balance.entity.AssetTransaction;
import com.kmbbj.backend.balance.entity.ChangeType;
import com.kmbbj.backend.balance.entity.TotalBalance;
import com.kmbbj.backend.balance.repository.totalbalances.TotalBalancesRepository;
import com.kmbbj.backend.balance.repository.transaction.AssetTransactionRepository;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RewardService {
    private final TotalBalancesRepository totalBalancesRepository;
    private final AssetTransactionRepository assetTransactionRepository;
    private final UserRepository userRepository;

    /**
     * 특정 유저에게 보상을 지급합니다.
     *
     * @param userId       유저 id
     * @param rewardAmount 보상 금액
     * @param changeType   보상 유형 이 메서드의 경우 BONUS 로 통일
     * @throws ApiException 유저를 찾을 수 없거나 보상 지급에 문제가 발생한 경우 예외 발생
     */
    @Transactional
    public void rewardUser(Long userId, Long rewardAmount, ChangeType changeType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ExceptionEnum.NOT_FOUND_USER)); // 유저 검색 실패

        TotalBalance totalBalance = totalBalancesRepository.findByUserId(userId) // id를 통해 총 금액 검색

                .orElseGet(() -> { // 해당 아이디의 총 금액 테이블이 없을 경우 새로 만든다
                    TotalBalance newTotalBalance = new TotalBalance();
                    newTotalBalance.setUser(user);
                    newTotalBalance.setAsset(0L); //초기값 0 설정
                    return totalBalancesRepository.save(newTotalBalance);
                });

        if (rewardAmount <= 0) {
            throw new ApiException(ExceptionEnum.INVALID_AMOUNT); // 보상 금액이 0 이하인 경우
        }

        try {
            totalBalance.changeAsset(rewardAmount);  // 보상 금액 + 자산 = totalBalance
            totalBalancesRepository.save(totalBalance); // totalBalance 저장

            AssetTransaction assetTransaction = AssetTransaction.builder()
                    .changeType(changeType) // 컨트롤러에서 BONUS 로 설정
                    .changeAmount(rewardAmount) // 보상 금액
                    .totalBalance(totalBalance) // 자산
                    .createTime(LocalDateTime.now())  // 현재 시간으로 설정
                    .build();
            assetTransactionRepository.save(assetTransaction); // 저장

        } catch (Exception e) {
            throw new ApiException(ExceptionEnum.INTERNAL_SERVER_ERROR);
        }
    }
}
