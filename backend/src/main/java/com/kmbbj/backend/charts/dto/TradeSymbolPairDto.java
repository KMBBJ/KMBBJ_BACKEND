package com.kmbbj.backend.charts.dto;

import com.google.gson.JsonArray;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TradeSymbolPairDto {
    private String symbol;
    private JsonArray trades;
}