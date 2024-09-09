package com.kmbbj.backend.feature.charts.controller;

import com.kmbbj.backend.feature.charts.entity.kline.Kline;
import com.kmbbj.backend.feature.charts.service.ChartService;
import com.kmbbj.backend.global.config.reponse.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chart")
public class ChartController {
    private final ChartService chartService;
    /**
     * 모든 코인의 차트 데이터를 업데이트
     */
    @PostMapping("/update")
    @Operation(summary = "차트 데이터 업데이트", description = "모든 코인의 지난 10시간 동안의 Kline 데이터를 업데이트합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "차트 데이터 업데이트 성공"),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 코인 코드"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public CustomResponse<String> updateAllChartData() {
        chartService.updateKlineDataForAllCoins();
        return new CustomResponse<>(HttpStatus.CREATED, "차트 데이터 업데이트 성공", null);
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
    public CustomResponse<List<Kline>> getKlineOfSymbol(@PathVariable String symbol, @PathVariable String interval) {
        return new CustomResponse<>(HttpStatus.CREATED, "차트 데이터 불러오기 성공", chartService.getKline(symbol, interval));
    }

    /**
     * 가장 최근 kline 차트 데이터 가져옴
     * @param symbol 코인의 symbol(코인/거래기준)
     * @return symbol과 일치하는 가장 최근의 kline 데이터
     */
    @Operation(summary = "최근 차트 데이터 불러오기", description = "매개 변수 symbol에 해당하는 코인의 가장 최근 차트 데이터를 불러옵니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "차트 데이터를 불러오기 성공"),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 코인 코드"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{symbol}/latest")
    public CustomResponse<Kline> getLatestKline(@PathVariable String symbol) {
        // 최신 데이터를 업데이트하고 가져옴
        return new CustomResponse<>(HttpStatus.CREATED, "최신 차트 데이터 불러오기 성공", chartService.getLatestKline(symbol));
    }
}