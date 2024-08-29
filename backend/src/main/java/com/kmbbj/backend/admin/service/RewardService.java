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
     * 특정 유저에게 보상 지급
     *
     * @param userId       유저 ID
     * @param rewardAmount 보상 금액
     * @param changeType   보상 유형 (GAME, BONUS)
     * @throws ApiException 유저를 찾을 수 없거나 보상 지급에 문제가 발생한 경우 발생
     */
    @Transactional
    public void rewardUser(Long userId, Long rewardAmount, ChangeType changeType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ExceptionEnum.NOT_FOUND_USER));

        TotalBalance totalBalance = totalBalancesRepository.findByUserId(userId)
                .orElseGet(() -> {
                    TotalBalance newTotalBalance = new TotalBalance();
                    newTotalBalance.setUser(user);
                    newTotalBalance.setAsset(0L);
                    return totalBalancesRepository.save(newTotalBalance);
                });

        if (rewardAmount <= 0) {
            throw new ApiException(ExceptionEnum.INVALID_AMOUNT);
        }

        try {
            totalBalance.changeAsset(rewardAmount);
            totalBalancesRepository.save(totalBalance);

            AssetTransaction assetTransaction = AssetTransaction.builder()
                    .changeType(changeType)
                    .changeAmount(rewardAmount)
                    .totalBalance(totalBalance)
                    .createTime(LocalDateTime.now())  // 현재 시간으로 설정
                    .build();
            assetTransactionRepository.save(assetTransaction);

        } catch (Exception e) {
            throw new ApiException(ExceptionEnum.INTERNAL_SERVER_ERROR);
        }
    }
}
