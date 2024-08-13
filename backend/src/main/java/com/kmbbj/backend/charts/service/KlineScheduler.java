package com.kmbbj.backend.charts.service;

import com.kmbbj.backend.charts.entity.coin.Coin;
import com.kmbbj.backend.charts.repository.coin.CoinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KlineScheduler {
    private final CoinRepository coinRepository;
    private final ChartService chartService;

    public void updateKlineDataByInterval(String interval) {
        List<Coin> coins = coinRepository.findAll();
        for (Coin coin : coins) {
            chartService.updateKlineData(coin.getSymbol(), interval);
        }
    }
    /**
     * 1분 간격으로 Kline 데이터를 자동으로 가져오는 메서드
     */
//    @Scheduled(cron = "0 */1 * * * *") // 매 1분마다 실행
    public void updateKlineDataEvery1Minute() {
        updateKlineDataByInterval("1m");
    }

    /**
     * 3분 간격으로 Kline 데이터를 자동으로 가져오는 메서드
     */
//    @Scheduled(cron = "0 */3 * * * *") // 매 3분마다 실행
    public void updateKlineDataEvery3Minutes() {
        updateKlineDataByInterval("3m");
    }

    /**
     * 5분 간격으로 Kline 데이터를 자동으로 가져오는 메서드
     */
//  @Scheduled(cron = "0 */5 * * * *") // 매 5분마다 실행
    public void updateKlineDataEvery5Minutes() {
        updateKlineDataByInterval("5m");
    }

    /**
     * 30분 간격으로 Kline 데이터를 자동으로 가져오는 메서드
     */
//    @Scheduled(cron = "0 */30 * * * *") // 매 30분마다 실행
    public void updateKlineDataEvery30Minutes() {
        updateKlineDataByInterval("30m");
    }

    /**
     * 하루 간격으로 Kline 데이터를 자동으로 가져오는 메서드
     */
//    @Scheduled(cron = "0 0 0 * * *") // 매일 00:00에 실행
    public void updateKlineDataEveryDay() {
        updateKlineDataByInterval("1d");
    }

    /**
     * 일주일 간격으로 Kline 데이터를 자동으로 가져오는 메서드
     */
//    @Scheduled(cron = "0 0 0 * * MON") // 매주 월요일 00:00에 실행
    public void updateKlineDataEveryWeek() {
        updateKlineDataByInterval("1w");
    }
}
