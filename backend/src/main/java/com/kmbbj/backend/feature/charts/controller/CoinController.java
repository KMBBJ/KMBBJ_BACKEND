package com.kmbbj.backend.feature.charts.controller;

import com.kmbbj.backend.feature.charts.dto.CoinResponse;
import com.kmbbj.backend.feature.charts.entity.CoinStatus;
import com.kmbbj.backend.feature.charts.service.CoinService;
import com.kmbbj.backend.global.config.reponse.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/coin")
public class CoinController {
    private final CoinService coinService;
    private final PagedResourcesAssembler<CoinResponse> pagedResourcesAssembler;

    /**
     * 코인 상세 정보 확인
     * @param symbol 코인의 symbol(코인/거래기준)
     * @return symbol과 일치하는 코인의 정보(Coin, CoinDetail)
     */
    @GetMapping("/detail/{symbol}")
    @Operation(summary = "코인 차트 및 코인 정보 확인", description = "매개변수 symbol에 해당하는 코인의 정보를 확인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "코인 정보 불러오기 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public CustomResponse<CoinResponse> getCoinDetail(@PathVariable("symbol") String symbol) {
        CoinResponse coinResponse = coinService.getCoinResponse(symbol);

        return new CustomResponse<>(HttpStatus.OK, "코인 정보 불러오기 성공", coinResponse);
    }

    /**
     * 코인 등록
     * @param symbol 코인의 심볼 (예: BTCUSDT, ETHUSDT)
     * @param coinName 코인 이름
     * @return 코인 등록 결과
     */
    @PutMapping("/add")
    @Operation(summary = "코인 추가", description = "새로운 코인을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "코인 등록 성공"),
            @ApiResponse(responseCode = "400", description = "이미 존재하는 코인")
    })
    public CustomResponse<String> addCoin(@RequestParam String symbol, @RequestParam String coinName) {
        coinService.addCoin(symbol, coinName);

        return new CustomResponse<>(HttpStatus.CREATED, coinName + "코인 등록이 완료되었습니다.", null);
    }

    /**
     * 코인 리스트 가져옴
     * @param pageNo 페이지 번호
     * @param size 페이지별 아이템 수
     * @param orderBy 정렬 기준
     * @param sort 정렬 방향
     * @param searchQuery 검색할 코인 명
     * @return 코인 리스트를 정렬 기준대로 정렬해서 페이지 모델로 반환
     */
    @GetMapping("/listPage")
    @Operation(summary = "코인, 코인 정보 리스트 가져옴", description = "코인, 코인 정보 리스트를 페이지 형식으로 가져옴")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "코인 리스트 반환 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public CustomResponse<PagedModel<EntityModel<CoinResponse>>> getCoinList(@RequestParam int pageNo, @RequestParam int size,
                                                          @RequestParam String orderBy, @RequestParam String sort,
                                                                             @RequestParam(required = false) String searchQuery) {
        Page<CoinResponse> coinPage = coinService.getPageCoins(pageNo, size, orderBy, sort, searchQuery);
        PagedModel<EntityModel<CoinResponse>> pagedModel = pagedResourcesAssembler.toModel(coinPage);
        return new CustomResponse<>(HttpStatus.OK, "페이지 반환 성공", pagedModel);
    }

    /**
     * 코인 삭제
     * @param symbol 코인의 심볼 (예: BTCUSDT, ETHUSDT)
     * @return 삭제 결과 메시지
     */
    @PutMapping("/delete/{symbol}")
    @Operation(summary = "코인 삭제", description = "심볼과 일치하는 코인을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "코인 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "코인을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public CustomResponse<String> deleteCoin(@PathVariable String symbol) {
        coinService.deleteCoin(symbol);
        return new CustomResponse<>(HttpStatus.OK, symbol + " 코인이 삭제되었습니다.", null);
    }

    /**
     * 코인 상세 정보 업데이트
     * @param symbol 코인의 심볼 (예: BTCUSDT, ETHUSDT)
     * @param status 새로운 상태 값
     * @return 업데이트 결과 메시지
     */
    @PutMapping("/update/{symbol}")
    @Operation(summary = "코인 상세 정보 업데이트", description = "심볼을 기반으로 코인의 상세 정보를 업데이트합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "코인 상세 정보 업데이트 성공"),
            @ApiResponse(responseCode = "400", description = "코인을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public CustomResponse<String> updateCoinDetail(@PathVariable String symbol,
            @RequestParam CoinStatus status) {
        coinService.updateCoin(symbol, status);
        return new CustomResponse<>(HttpStatus.OK, symbol + " 코인의 상세 정보가 업데이트되었습니다.", null);
    }
}
