package com.kmbbj.backend.feature.charts.repository.kline;

import com.kmbbj.backend.feature.charts.entity.coin.Coin;
import com.kmbbj.backend.feature.charts.entity.kline.Kline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KlineRepository extends JpaRepository<Kline, Long> {
    List<Kline> findAllByCoinAndInterval(Coin coin, String interval);
    Kline findTopByCoinAndIntervalOrderByTimezoneDesc(Coin coin, String interval);
    boolean  existsByCoinAndIntervalAndTimezone(Coin coin, String interval, Long timezone);
}
