package com.kmbbj.backend.charts.service;

import com.google.gson.JsonArray;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface BinanceApiService {
    void updateCoinData();

    Mono<String> getKlines(String symbol, String interval, Long startTime, Long endTime, Integer limit);

    Mono<JsonArray> getRecentlyTrade(String symbol);

    Mono<List<Map<String, Object>>> getBookTicker(List<String> symbols);
}
