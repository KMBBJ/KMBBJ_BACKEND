package com.kmbbj.backend.feature.charts.event;

import com.kmbbj.backend.feature.charts.dto.CoinResponse;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import java.util.List;

@Getter
public class CoinDetailUpdateEvent extends ApplicationEvent {
    private final List<CoinResponse> updatedCoinDetails;

    public CoinDetailUpdateEvent(Object source, List<CoinResponse> updatedCoinDetails) {
        super(source);
        this.updatedCoinDetails = updatedCoinDetails;
    }
}