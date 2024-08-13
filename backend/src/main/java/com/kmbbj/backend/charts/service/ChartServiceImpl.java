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
        // 코인 심볼로 코인을 검색, 존재하지 않으면 예외 발생
        Coin coin = coinRepository.findBySymbol(symbol).orElseThrow(() -> new ApiException(ExceptionEnum.NOT_FOUND_SYMBOL));
        // 해당 코인과 시간 간격에 맞는 Kline 데이터를 반환
        return klineRepository.findAllByCoinAndInterval(coin, interval);
    }

    /**
     * Coin 테이블에 있는 모든 코인 심볼에 대해 Kline 데이터를 받아와서 저장
     */
    @Override
    public void updateKlineDataForAllCoins() {
        // 업데이트 할 모든 코인 리스트를 가져옴
        List<Coin> coins = coinRepository.findAll();
        // 업데이트할 시간 간격 배열 설정
        String[] intervals = {"1m", "3m", "5m", "30m", "1d", "1w"};
        // 각 코인에 대해 모든 시간 간격의 Kline 데이터를 업데이트
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
        // 심볼에 USDT를 붙여서 Binance API에서 사용하는 형식으로 만듦
        String symbolWithUSDT = coin.getSymbol() + "USDT";
        // Binance API를 통해 Kline 데이터를 가져옴, limit은 최근 30개의 데이터를 가져오도록 설정
        Mono<String> klineData = binanceApiService.getKlines(symbolWithUSDT, interval, null, null, 30);
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
        // 종가 데이터를 저장할 리스트
        List<Double> closePrices = new ArrayList<>();

        for (JsonElement element : klines) {
            JsonArray kline = element.getAsJsonArray();

            long timezone = kline.get(0).getAsLong(); // Unix timestamp
            double openPrice = kline.get(1).getAsDouble(); // 시가
            double closePrice = kline.get(4).getAsDouble(); // 종가
            double highPrice = kline.get(2).getAsDouble(); // 고가
            double lowPrice = kline.get(3).getAsDouble(); // 저가
            double volume = kline.get(5).getAsDouble(); // 거래량

            // 종가 리스트에 추가
            closePrices.add(closePrice);

            // interval이 1d일 때만 이동평균과 볼린저 밴드 계산
            Double ma10 = null;
            Double ma20 = null;
            Double ma30 = null;
            Double bbu = null;
            Double bbd = null;

            if ("1d".equals(interval)) {
                ma10 = calculateMovingAverage(closePrices, 10); // 10일 이동평균
                ma20 = calculateMovingAverage(closePrices, 20); // 20일 이동평균
                ma30 = calculateMovingAverage(closePrices, 30); // 30일 이동평균

                double[] bollingerBands = calculateBollingerBands(closePrices, 20);
                bbu = bollingerBands[0]; // 상한선
                bbd = bollingerBands[1]; // 하한선
            }
            // Kline 데이터를 엔티티로 변환하여 저장
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
     * @param period 이동평균을 계산할 기간 (예: 10일, 20일 등)
     * @return 특정 기간 동안의 종가의 평균을 계산하여 이동 평균을 반환
     */
    public static double calculateMovingAverage(List<Double> prices, int period) {
        // 데이터가 충분하지 않으면 0 반환
        if (prices.size() < period) {
            return 0.0;
        }
        double sum = 0.0;
        // 마지막 period 기간의 가격 합을 계산
        for (int i = prices.size() - period; i < prices.size(); i++) {
            sum += prices.get(i);
        }
        // 평균을 반환
        return sum / period;
    }

    /**
     * 표준편차 계산
     * @param prices 계산할 종가 모음
     * @param mean 종가의 평균값
     * @param period 표준편차를 계산할 기간 (예: 20일 등)
     * @return 특정 기간 동안의 종가의 평균으로부터의 표준편차를 계산하여 반환
     */
    public static double calculateStandardDeviation(List<Double> prices, double mean, int period) {
        // 데이터가 충분하지 않으면 0 반환
        if (prices.size() < period) {
            return 0.0;
        }
        double sum = 0.0;
        // 각 가격이 평균에서 얼마나 떨어져 있는지를 제곱하여 합산
        for (int i = prices.size() - period; i < prices.size(); i++) {
            sum += Math.pow(prices.get(i) - mean, 2);
        }
        // 분산의 제곱근인 표준편차를 계산하여 반환
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
        // 주어진 기간에 대한 이동 평균 계산
        double mean = calculateMovingAverage(prices, period);
        // 이동 평균에서의 표준편차 계산
        double stdDev = calculateStandardDeviation(prices, mean, period);
        // 상한선 (이동 평균 + 2 * 표준편차)
        double upperBand = mean + 2 * stdDev;
        // 하한선 (이동 평균 - 2 * 표준편차)
        double lowerBand = mean - 2 * stdDev;
        // 상한선과 하한선을 배열로 반환
        return new double[]{upperBand, lowerBand};
    }
}
