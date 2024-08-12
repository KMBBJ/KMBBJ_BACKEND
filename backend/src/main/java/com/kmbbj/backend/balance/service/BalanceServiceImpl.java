package com.kmbbj.backend.balance.service;

import com.kmbbj.backend.balance.entity.TotalBalance;
import com.kmbbj.backend.balance.service.totalbalance.TotalBalanceService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BalanceServiceImpl implements BalanceService {
    private final TotalBalanceService totalBalanceService;

    public BalanceServiceImpl(@Qualifier("totalBalanceServiceImpl")TotalBalanceService totalBalanceService) {
        this.totalBalanceService = totalBalanceService;
    }

    @Override
    public Optional<TotalBalance> totalBalanceFindByUserId(Long userId) {
        return totalBalanceService.totalBalanceFindByUserId(userId);
    }

    @Override
    public void makeTotalBalance(TotalBalance totalBalance){
        totalBalanceService.makeTotalBalance(totalBalance);
    }
}