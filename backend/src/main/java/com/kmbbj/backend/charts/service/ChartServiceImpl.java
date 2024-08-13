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
     * @param symbol 코인의 심볼 (예: BTCUSDT, ETHUSDT)
     * @param interval 시간 간격
     * @return 심볼과 시간 간격을 조건으로 찾은 kline 데이터 리스트
     */
    public List<Kline> getKline(String symbol, String interval) {
        Coin coin = coinRepository.findBySymbol(symbol).orElseThrow(() -> new ApiException(ExceptionEnum.NOT_FOUND_SYMBOL));
        return klineRepository.findAllByCoinAndInterval(coin, interval);
    }

    /**
     * Coin 테이블에 있는 모든 코인 심볼에 대해 Kline 데이터를 받아와서 저장
     */
    @Override
    public void updateKlineDataForAllCoins() {
        List<Coin> coins = coinRepository.findAll();
        String[] intervals = {"1m", "3m", "5m", "30m", "1d", "1w"};

        for (Coin coin : coins) {
            for (String interval : intervals) {
                updateKlineData(coin.getSymbol(), interval);
            }
        }
    }

    /**
     * 심볼에 대한 kline 데이터를 받아와서 saveKlineData에 넘겨줌
     * @param symbol 코인의 심볼 (예: BTCUSDT, ETHUSDT)
     * @param interval 시간 간격
     */
    public void updateKlineData(String symbol, String interval) {
        Coin coin = coinRepository.findBySymbol(symbol).orElseThrow(() -> new ApiException(ExceptionEnum.NOT_FOUND_SYMBOL));
        String symbolWithUSDT = coin.getSymbol() + "USDT";
        Mono<String> klineData = binanceApiService.getKlines(symbolWithUSDT, interval, null, null, 30);

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
        List<Double> closePrices = new ArrayList<>();

        for (JsonElement element : klines) {
            JsonArray kline = element.getAsJsonArray();

            long timezone = kline.get(0).getAsLong();
            double openPrice = kline.get(1).getAsDouble();
            double closePrice = kline.get(4).getAsDouble();
            double highPrice = kline.get(2).getAsDouble();
            double lowPrice = kline.get(3).getAsDouble();
            double volume = kline.get(5).getAsDouble();

            closePrices.add(closePrice);

            // interval이 1d일 때만 이동평균과 볼린저 밴드 계산
            Double ma10 = null;
            Double ma20 = null;
            Double ma30 = null;
            Double bbu = null;
            Double bbd = null;

            if ("1d".equals(interval)) {
                ma10 = calculateMovingAverage(closePrices, 10);
                ma20 = calculateMovingAverage(closePrices, 20);
                ma30 = calculateMovingAverage(closePrices, 30);

                double[] bollingerBands = calculateBollingerBands(closePrices, 20);
                bbu = bollingerBands[0];
                bbd = bollingerBands[1];
            }

            Kline klineData = Kline.builder()
                    .interval(interval)
                    .openPrice(openPrice)
                    .closePrice(closePrice)
                    .highPrice(highPrice)
                    .lowPrice(lowPrice)
                    .volume(volume)
                    .timezone(timezone)
                    .coin(coin)
                    .ma10(ma10)
                    .ma20(ma20)
                    .ma30(ma30)
                    .bbu(bbu)
                    .bbd(bbd)
                    .build();

            klineRepository.save(klineData);
        }
    }

    /**
     * 이동평균 계산
     * @param prices 계산할 종가 모음
     * @param period 기간
     * @return 특정 기간 동안의 종가의 평균을 계산하여 이동 평균을 반환
     */
    public static double calculateMovingAverage(List<Double> prices, int period) {
        if (prices.size() < period) {
            return 0.0;
        }
        double sum = 0.0;
        for (int i = prices.size() - period; i < prices.size(); i++) {
            sum += prices.get(i);
        }
        return sum / period;
    }

    /**
     * 표준편차 계산
     * @param prices 계산할 종가 모음
     * @param mean 종가의 평균값
     * @param period 기간
     * @return 특정 기간 동안의 종가의 평균으로부터의 표준편차를 계산하여 반환
     */
    public static double calculateStandardDeviation(List<Double> prices, double mean, int period) {
        if (prices.size() < period) {
            return 0.0;
        }
        double sum = 0.0;
        for (int i = prices.size() - period; i < prices.size(); i++) {
            sum += Math.pow(prices.get(i) - mean, 2);
        }
        return Math.sqrt(sum / period);
    }

    /**
     * 볼린저 밴드 계산
     * @param prices 계산할 종가 모음
     * @param period 기간
     * @return 볼린저 밴드를 계산하여 상한선과 하한선을 배열로 반환.
     *         배열의 첫 번째 요소는 상한선(Upper Band), 두 번째 요소는 하한선(Lower Band).
     */
    public static double[] calculateBollingerBands(List<Double> prices, int period) {
        double mean = calculateMovingAverage(prices, period);
        double stdDev = calculateStandardDeviation(prices, mean, period);
        double upperBand = mean + 2 * stdDev;
        double lowerBand = mean - 2 * stdDev;
        return new double[]{upperBand, lowerBand};
    }
}
