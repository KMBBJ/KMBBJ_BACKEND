package com.kmbbj.backend.feature.games.repository;

import com.kmbbj.backend.feature.games.entity.CoinBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoinBalanceRepository extends JpaRepository<CoinBalance, Long> {
    Optional<CoinBalance> findCoinBalanceByGameBalanceIdAndCoinId(Long gameBalanceId, Long coinId);
    List<CoinBalance> findALLCoinBalanceByGameBalanceId(Long gameBalanceId);
}