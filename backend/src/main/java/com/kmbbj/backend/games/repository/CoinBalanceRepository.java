package com.kmbbj.backend.games.repository;

import com.kmbbj.backend.games.entity.CoinBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CoinBalanceRepository extends JpaRepository<CoinBalance, Long> {
    Optional<CoinBalance> findCoinBalanceByGameBalanceIdAndCoinId(Long gameBalanceId, Long coinId);
}
