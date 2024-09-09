package com.kmbbj.backend.feature.charts.event;

import com.kmbbj.backend.feature.charts.entity.coin.Coin;
import com.kmbbj.backend.feature.charts.repository.coin.CoinRepository;
import com.kmbbj.backend.feature.charts.service.ChartService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KlineScheduler {
    private final CoinRepository coinRepository;
    private final ChartService chartService;

    @Value("${API_RECENTLY_UPDATE_LIMIT}")
    private Integer limit;

    @Value("${API_INTERVAL}")
    private String interval;

    public void updateKlineDataByInterval(String interval) {
        List<Coin> coins = coinRepository.findAll();

        for (Coin coin : coins) {
            chartService.updateKlineData(coin.getSymbol(), interval, limit);
        }
    }
    /**
     * 매 5분 간격으로 Kline 데이터를 자동으로 가져오는 메서드
     */
//    @Scheduled(cron = "0 */5 * * * *") // 매 5분마다 실행
//    public void updateKlineDataEvery5Minutes() {
//        updateKlineDataByInterval(interval);
//    }
}
