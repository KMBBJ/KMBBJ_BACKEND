package com.kmbbj.backend.charts.event;

import com.kmbbj.backend.charts.dto.CoinResponse;
import lombok.Getter;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Component
public class CoinUpdateEventListener {
    private List<CoinResponse> latestCoinDetails;

    @EventListener
    public void handleCoinDetailUpdateEvent(CoinDetailUpdateEvent event) {
        // 이벤트가 발생할 때 최신 코인 디테일 정보를 저장
        latestCoinDetails = event.getUpdatedCoinDetails();
    }
}