package com.kmbbj.backend.charts.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.kmbbj.backend.charts.entity.coin.Coin;
import com.kmbbj.backend.charts.entity.kline.*;
import com.kmbbj.backend.charts.repository.coin.CoinRepository;
import com.kmbbj.backend.charts.repository.kline.*;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChartServiceImpl implements ChartService {
    private final KlineRepository klineRepository;
    private final CoinRepository coinRepository;
    private final BinanceApiService binanceApiService;

    /**
     * 코인 심볼과 시간 간격이 일치하는 Kline 데이터를 가져옴
     *
     * @param symbol   코인의 심볼 (예: BTCUSDT, ETHUSDT)
     * @param interval 시간 간격
     * @return 심볼과 시간 간격을 조건으로 찾은 kline 데이터 리스트
     */
    public List<Kline> getKline(String symbol, String interval) {
        // 코인 심볼로 코인을 검색, 존재하지 않으면 예외 발생
        Coin coin = coinRepository.findBySymbol(symbol).orElseThrow(() -> new ApiException(ExceptionEnum.NOT_FOUND_SYMBOL));
        // 해당 코인과 시간 간격에 맞는 Kline 데이터를 반환
        return klineRepository.findAllByCoinAndInterval(coin, interval);
    }

    /**
     * 코인 심볼과 시간 간격에 따라 가장 최근의 30분 Kline 데이터를 반환
     *
     * @param symbol 코인의 심볼 (예: BTCUSDT, ETHUSDT)
     * @return 최신 Kline 데이터 리스트
     */
    public Kline getLatestKline(String symbol) {
        // 코인 심볼로 코인을 검색, 존재하지 않으면 예외 발생
        Coin coin = coinRepository.findBySymbol(symbol).orElseThrow(() -> new ApiException(ExceptionEnum.NOT_FOUND_SYMBOL));

        // 업데이트된 데이터를 바로 가져옴 (가장 최근의 1개 데이터)
        return klineRepository.findTopByCoinAndIntervalOrderByTimezoneDesc(coin, "5m");
    }

    /**
     * Coin 테이블에 있는 모든 코인 심볼에 대해 Kline 데이터를 받아와서 저장
     */
    @Override
    public void updateKlineDataForAllCoins() {
        // 업데이트 할 모든 코인 리스트를 가져옴
        List<Coin> coins = coinRepository.findAll();
        // 각 코인에 대해 모든 시간 간격의 Kline 데이터를 업데이트
        // 코인 리스트를 병렬 스트림으로 처리하여 병렬로 Kline 데이터를 업데이트
        coins.forEach(coin -> {
            updateKlineData(coin.getSymbol(), "5m", 120);
        });
    }

    /**
     * 심볼에 대한 kline 데이터를 받아와서 saveKlineData에 넘겨줌
     *
     * @param symbol   코인의 심볼 (예: BTCUSDT, ETHUSDT)
     * @param interval 시간 간격
     * @param limit    갯수 제한
     */
    public void updateKlineData(String symbol, String interval, Integer limit) {
        Coin coin = coinRepository.findBySymbol(symbol).orElseThrow(() -> new ApiException(ExceptionEnum.NOT_FOUND_SYMBOL));
        // 심볼에 USDT를 붙여서 Binance API에서 사용하는 형식으로 만듦
        String symbolWithUSDT = coin.getSymbol() + "USDT";
        // 현재 시간을 밀리초 단위로 Unix 타임스탬프 형식으로 얻음

        // Binance API를 통해 Kline 데이터를 가져옴
        Mono<String> klineData = binanceApiService.getKlines(symbolWithUSDT, interval, null, null, limit);

        // 데이터를 파싱하여 저장
        klineData.map(JsonParser::parseString)
                .map(JsonElement::getAsJsonArray)
                .doOnNext(klines -> saveKlineData(interval, klines, coin))
                .subscribe();
    }

    /**
     * klines 데이터를 각 시간대 별로 DB에 저장
     * @param interval 시간 간격
     * @param klines kline 데이터
     * @param coin kline 데이터를 찾을 코인 정보
     */
    public void saveKlineData(String interval, JsonArray klines, Coin coin) {
        List<Kline> klineDataList = new ArrayList<>();

        for (JsonElement element : klines) {
            JsonArray kline = element.getAsJsonArray();

            long timezone = kline.get(0).getAsLong(); // Unix timestamp
            // Unix 타임스탬프를 LocalDateTime으로 변환하고, 한국 시간으로 변환
            LocalDateTime dateTimeKST = LocalDateTime.ofInstant(Instant.ofEpochMilli(timezone), ZoneId.of("Asia/Seoul"));

            // 다시 Unix 타임스탬프(long 타입)으로 변환
            long timezoneKST = dateTimeKST.toInstant(ZoneOffset.UTC).toEpochMilli();

            double openPrice = kline.get(1).getAsDouble(); // 시가
            double closePrice = kline.get(4).getAsDouble(); // 종가
            double highPrice = kline.get(2).getAsDouble(); // 고가
            double lowPrice = kline.get(3).getAsDouble(); // 저가
            double volume = kline.get(5).getAsDouble(); // 거래량

            // 동일한 timezone과 interval을 가진 데이터가 이미 있는지 확인
            boolean exists = klineRepository.existsByCoinAndIntervalAndTimezone(coin, interval, timezoneKST);
            if (exists) {
                continue; // 이미 존재하는 데이터는 저장하지 않음
            }

            // Kline 데이터를 엔티티로 변환하여 저장
            Kline klineData = Kline.builder()
                    .interval(interval)
                    .openPrice(openPrice)
                    .closePrice(closePrice)
                    .highPrice(highPrice)
                    .lowPrice(lowPrice)
                    .volume(volume)
                    .timezone(timezoneKST)
                    .coin(coin)
                    .build();

            klineDataList.add(klineData);
        }

        // Batch Insert를 통해 한 번에 저장
        klineRepository.saveAll(klineDataList);
    }
}
