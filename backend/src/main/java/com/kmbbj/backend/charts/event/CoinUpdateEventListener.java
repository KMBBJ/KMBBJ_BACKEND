package com.kmbbj.backend.charts.event;

import com.kmbbj.backend.charts.dto.CoinResponse;
import com.kmbbj.backend.feature.exchange.service.execution.matching.ExecutionAllMatchingOrder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Component
public class CoinUpdateEventListener {
    private final ExecutionAllMatchingOrder executionAllMatchingOrder;

    public CoinUpdateEventListener(@Qualifier("executionAllMatchingOrderImpl") ExecutionAllMatchingOrder executionAllMatchingOrder) {
        this.executionAllMatchingOrder = executionAllMatchingOrder;
    }

    private List<CoinResponse> latestCoinDetails;

    @EventListener
    public void handleCoinDetailUpdateEvent(CoinDetailUpdateEvent event) {
        // 이벤트가 발생할 때 최신 코인 디테일 정보를 저장
        latestCoinDetails = event.getUpdatedCoinDetails();

        // 이벤트 발생 시 matchOrders 서비스 호출
        for (CoinResponse coinResponse : latestCoinDetails) {
            // coinId와 price를 기반으로 매칭 로직 실행
            Long coinId = coinResponse.getCoin().getCoinId();
            Long price = (long) coinResponse.getCoin24hDetail().getPrice();
            executionAllMatchingOrder.matchOrders(coinId, price);
        }
    }
}