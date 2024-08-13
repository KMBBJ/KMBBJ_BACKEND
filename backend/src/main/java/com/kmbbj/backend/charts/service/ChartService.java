package com.kmbbj.backend.charts.service;

import com.kmbbj.backend.charts.entity.kline.Kline;

import java.util.List;

public interface ChartService {
    List<Kline> getKline(String symbol, String interval);
    void updateKlineDataForAllCoins();
    void updateKlineData(String symbol, String interval);
}
