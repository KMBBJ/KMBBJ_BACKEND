package com.kmbbj.backend.matching.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
public enum StartSeedMoney {
    TEN_MILLION(10000000),
    TWENTY_MILLION(20000000),
    THIRTY_MILLION(30000000),
    FORTY_MILLION(40000000);

    private final int amount;
}
