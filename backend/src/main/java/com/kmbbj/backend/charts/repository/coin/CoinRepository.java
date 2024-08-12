package com.kmbbj.backend.charts.repository.coin;

import com.kmbbj.backend.charts.entity.coin.Coin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CoinRepository extends JpaRepository<Coin, Long> {
    Optional<Coin> findBySymbol(String symbol);
    Optional<Coin> findByCoinName(String coinName);
    Page<Coin> findAll(Pageable pageable);
}
