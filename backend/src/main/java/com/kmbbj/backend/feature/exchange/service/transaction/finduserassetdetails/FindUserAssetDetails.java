package com.kmbbj.backend.feature.exchange.service.transaction.finduserassetdetails;

import com.kmbbj.backend.feature.exchange.controller.response.UserAssetResponse;

public interface FindUserAssetDetails {
    UserAssetResponse findUserAssetDetails(Long userId);
}
