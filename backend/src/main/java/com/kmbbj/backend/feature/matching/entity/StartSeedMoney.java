package com.kmbbj.backend.feature.matching.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
public enum StartSeedMoney {
    THREE_MILLION(3000000),
    FIVE_MILLION(5000000),
    SEVEN_MILLION(7000000),
    TEN_MILLION(10000000),
    FIFTEEN_MILLION(15000000),
    TWENTY_MILLION(20000000),
    THIRTY_MILLION(30000000),
    FORTY_MILLION(40000000);

    private final int amount;
}
