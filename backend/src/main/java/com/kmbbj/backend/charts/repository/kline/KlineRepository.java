package com.kmbbj.backend.charts.repository.kline;

import com.kmbbj.backend.charts.entity.coin.Coin;
import com.kmbbj.backend.charts.entity.kline.Kline;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KlineRepository extends CrudRepository<Kline, Long> {
    List<Kline> findAllByCoinAndInterval(Coin coin, String interval);
}
