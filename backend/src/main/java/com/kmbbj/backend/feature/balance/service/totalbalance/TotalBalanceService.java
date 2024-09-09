package com.kmbbj.backend.feature.balance.service.totalbalance;

import com.kmbbj.backend.feature.balance.entity.TotalBalance;

import java.util.Optional;

public interface TotalBalanceService {
    Optional<TotalBalance> totalBalanceFindByUserId(Long userId);
    void makeTotalBalance(TotalBalance totalBalance);
}
