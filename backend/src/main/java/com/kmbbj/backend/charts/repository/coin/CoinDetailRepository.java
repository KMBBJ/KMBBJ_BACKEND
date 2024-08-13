package com.kmbbj.backend.charts.repository.coin;

import com.kmbbj.backend.charts.entity.coin.Coin;
import com.kmbbj.backend.charts.entity.coin.CoinDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CoinDetailRepository extends JpaRepository<CoinDetail, Long> {
    Optional<CoinDetail> findTopByCoinOrderByTimezoneDesc(Coin coin);
}
