package com.kmbbj.backend.feature.exchange.service.buy;

import com.kmbbj.backend.feature.exchange.service.buy.cansel.CanselBuyOrder;
import com.kmbbj.backend.feature.exchange.service.buy.save.SaveBuyOrder;

public interface Buy extends
        CanselBuyOrder,
        SaveBuyOrder
{ }