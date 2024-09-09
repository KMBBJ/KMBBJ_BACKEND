package com.kmbbj.backend.feature.charts.service;

import com.kmbbj.backend.feature.charts.entity.kline.Kline;

import java.util.List;

public interface ChartService {
    List<Kline> getKline(String symbol, String interval);
    Kline getLatestKline(String symbol);
    void updateKlineDataForAllCoins();
    void updateKlineData(String symbol, String interval, Integer limit);
}
