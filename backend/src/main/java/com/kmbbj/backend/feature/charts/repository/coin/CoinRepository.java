package com.kmbbj.backend.feature.charts.repository.coin;

import com.kmbbj.backend.feature.charts.entity.CoinStatus;
import com.kmbbj.backend.feature.charts.entity.coin.Coin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoinRepository extends JpaRepository<Coin, Long> {
    Optional<Coin> findBySymbol(String symbol);
    Optional<Coin> findByCoinName(String coinName);
    Page<Coin> findAll(Pageable pageable);
    List<Coin> findAllByStatus(CoinStatus status);
}
