package com.kmbbj.backend.charts.repository.coin;

import com.kmbbj.backend.charts.entity.coin.Coin;
import com.kmbbj.backend.charts.entity.coin.Coin24hDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface Coin24hDetailRepository extends JpaRepository<Coin24hDetail, Long> {
    Optional<Coin24hDetail> findTopByCoinOrderByTimezoneDesc(Coin coin);
}
