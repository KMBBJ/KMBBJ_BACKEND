package com.kmbbj.backend.matching.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AssetRangeCalculator {

    // 5분을 시간으로 변환 (5분 / 60분)
    @Value("${ASSET_RANGE_T}")
    private double T;

    // 목표 자산 범위
    @Value("${ASSET_RANGE_X}")
    private double X;



    // 자산 범위 계산 메서드
    public double calculateAssetRange(double t) {

        // b 값을 찾기 위해 목표 자산 범위 식을 재정리
        double desiredExp = (X - 5) / 15;

        // e^(-b * t) = 1 - desiredExp 식을 b에 대해 풀기
        double b = -Math.log(1 - desiredExp) / (T * t);
        return 5 + 15 * (1 - Math.exp(-b * (T * t)));
    }
}
