package com.kmbbj.backend.admin.service;


import com.kmbbj.backend.balance.entity.AssetTransaction;
import com.kmbbj.backend.balance.entity.TotalBalance;

import com.kmbbj.backend.balance.repository.totalbalances.TotalBalancesRepository;
import com.kmbbj.backend.balance.repository.transaction.AssetTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
@Service
@RequiredArgsConstructor
public class BalanceService {

    private final TotalBalancesRepository totalBalancesRepository;
    private final AssetTransactionRepository assetTransactionRepository;

    // 특정 사용자의 자산 정보를 가져오는 메서드
    @Transactional(readOnly = true)
    public TotalBalance getTotalBalanceByUserId(Long userId) {
        return totalBalancesRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Total balance not found for user ID: " + userId));
    }

    // 특정 사용자의 자산 변동 내역을 가져오는 메서드
    @Transactional(readOnly = true)
    public List<AssetTransaction> getAssetTransactionsByUserId(Long userId) {
        TotalBalance totalBalance = getTotalBalanceByUserId(userId);
        return assetTransactionRepository.findAllByTotalBalance_TotalBalanceId(totalBalance.getTotalBalanceId());
    }
}
