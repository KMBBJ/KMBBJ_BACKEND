package com.kmbbj.backend.charts.controller;

import com.kmbbj.backend.charts.entity.kline.Kline;
import com.kmbbj.backend.charts.service.ChartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chart")
public class ChartController {
    private final ChartService chartService;
    /**
     * 모든 코인의 데이터를 업데이트
     */
    @PostMapping("/update")
    @Operation(summary = "차트 데이터 업데이트", description = "모든 코인의 Kline 데이터를 업데이트합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "차트 데이터 업데이트 성공"),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 코인 코드"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    //    @Scheduled(fixedRate = 60000)
    public void updateAllCoinsData() {
        chartService.updateKlineDataForAllCoins();
    }

    /**
     * kline 차트 데이터 가져옴
     * @param symbol 코인의 symbol(코인/거래기준)
     * @param interval 시간 간격
     * @return symbol과 일치하는 kline 데이터
     */
    @GetMapping("/kline/{symbol}/{interval}")
    @Operation(summary = "차트 데이터 불러오기", description = "매개 변수 symbol에 해당하는 코인의 차트 데이터를 불러옵니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "차트 데이터를 불러오기 성공"),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 코인 코드"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public List<Kline> getKlineOfSymbol(@PathVariable String symbol, @PathVariable String interval) {
        return chartService.getKline(symbol, interval);
    }
}