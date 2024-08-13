package com.kmbbj.backend.charts.service;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface BinanceApiService {
    void updateCoinData();

    Mono<String> getKlines(String symbol, String interval, Long startTime, Long endTime, Integer limit);

    Mono<List<Map<String, Object>>> get24hrTickerData(List<String> symbols);
}
