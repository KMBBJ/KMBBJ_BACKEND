package com.kmbbj.backend.charts.event;

import com.kmbbj.backend.charts.entity.coin.Coin;
import com.kmbbj.backend.charts.repository.coin.CoinRepository;
import com.kmbbj.backend.charts.service.ChartService;
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
            chartService.updateKlineData(coin.getSymbol(), interval, 1);
        }
    }
    /**
     * 매 5분 간격으로 Kline 데이터를 자동으로 가져오는 메서드
     */
    //@Scheduled(cron = "0 */5 * * * *") // 매 5분마다 실행
    public void updateKlineDataEvery5Minutes() {
        updateKlineDataByInterval("5m");
    }
}
