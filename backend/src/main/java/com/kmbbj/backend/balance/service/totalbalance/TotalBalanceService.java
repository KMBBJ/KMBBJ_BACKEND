package com.kmbbj.backend.balance.service.totalbalance;

import com.kmbbj.backend.balance.entity.TotalBalance;

import java.util.Optional;

public interface TotalBalanceService {
    Optional<TotalBalance> totalBalanceFindByUserId(Long userId);
    void makeTotalBalance(TotalBalance totalBalance);
}
