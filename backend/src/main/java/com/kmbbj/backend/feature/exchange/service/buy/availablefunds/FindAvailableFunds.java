package com.kmbbj.backend.feature.exchange.service.buy.availablefunds;

import com.kmbbj.backend.feature.exchange.controller.request.AvailableBuyFundsRequest;
import com.kmbbj.backend.feature.exchange.controller.response.AvailableBuyFundsResponse;

public interface FindAvailableFunds {
    AvailableBuyFundsResponse findAvailableFunds(AvailableBuyFundsRequest request);
}
