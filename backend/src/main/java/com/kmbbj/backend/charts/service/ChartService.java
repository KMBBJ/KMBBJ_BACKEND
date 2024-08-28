package com.kmbbj.backend.charts.service;

import com.kmbbj.backend.charts.entity.kline.Kline;
import reactor.core.publisher.Flux;

import java.util.List;

public interface ChartService {
    List<Kline> getKline(String symbol, String interval);
    Kline getLatestKline(String symbol);
    void updateKlineDataForAllCoins();
    void updateKlineData(String symbol, String interval, Integer limit);
}
