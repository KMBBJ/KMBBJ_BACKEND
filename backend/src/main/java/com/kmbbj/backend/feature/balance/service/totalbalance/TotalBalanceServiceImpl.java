package com.kmbbj.backend.feature.balance.service.totalbalance;

import com.kmbbj.backend.feature.balance.entity.TotalBalance;
import com.kmbbj.backend.feature.balance.repository.totalbalances.TotalBalancesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TotalBalanceServiceImpl implements TotalBalanceService {
    private final TotalBalancesRepository totalBalancesRepository;

    public Optional<TotalBalance> totalBalanceFindByUserId(Long userId){
        return totalBalancesRepository.findByUserId(userId);
    }

    public void makeTotalBalance(TotalBalance totalBalance){
        totalBalancesRepository.save(totalBalance);
    }
}
