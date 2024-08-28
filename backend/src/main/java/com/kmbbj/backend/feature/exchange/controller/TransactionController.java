package com.kmbbj.backend.feature.exchange.controller;

import com.kmbbj.backend.feature.exchange.controller.request.*;
import com.kmbbj.backend.feature.exchange.controller.response.AvailableBuyFundsResponse;
import com.kmbbj.backend.feature.exchange.controller.response.AvailableSellCoinsResponse;
import com.kmbbj.backend.feature.exchange.controller.response.TransactionsResponse;
import com.kmbbj.backend.feature.exchange.controller.response.UserAssetResponse;
import com.kmbbj.backend.feature.exchange.service.TransactionService;
import com.kmbbj.backend.global.config.reponse.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "거래 관련 엔드포인트")
@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    /**
     * 매수 주문을 생성
     *
     * @param orderRequest 매수 주문 요청 데이터
     * @return 매수 주문 생성 결과
     */
    @PostMapping("/buy")
    @Operation(summary = "매수 주문 생성", description = "새로운 매수 주문을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "매수 주문 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
    })
    public CustomResponse<Void> createBuyOrder(@RequestBody OrderRequest orderRequest) {
        transactionService.saveBuyOrder(orderRequest);
        return new CustomResponse<>(HttpStatus.CREATED, "매수 주문이 성공적으로 생성되었습니다.", null);
    }

    /**
     * 매도 주문을 생성
     *
     * @param orderRequest 매도 주문 요청 데이터
     * @return 매도 주문 생성 결과
     */
    @PostMapping("/sell")
    @Operation(summary = "매도 주문 생성", description = "새로운 매도 주문을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "매도 주문 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
    })
    public CustomResponse<Void> createSellOrder(@RequestBody OrderRequest orderRequest) {
        transactionService.saveSellOrder(orderRequest);
        return new CustomResponse<>(HttpStatus.CREATED, "매도 주문이 성공적으로 생성되었습니다.", null);
    }

    /**
     * 주문을 취소
     *
     * @param canselRequest 취소할 주문 요청 데이터
     * @return 주문 취소 결과
     */
    @PostMapping("/cancel")
    @Operation(summary = "주문 취소", description = "기존 주문을 취소합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "주문 취소 성공"),
            @ApiResponse(responseCode = "404", description = "주문을 찾을 수 없음")
    })
    public CustomResponse<Void> cancelOrder(@RequestBody CanselRequest canselRequest) {
        transactionService.canselOrder(canselRequest);
        return new CustomResponse<>(HttpStatus.NO_CONTENT, "주문이 성공적으로 취소되었습니다.", null);
    }

    /**
     * 사용자의 거래 내역을 조회
     *
     * @param transactionsRequest 거래 내역 요청 데이터
     * @return 거래 내역 조회 결과
     */
    @PostMapping("/user/transactions")
    @Operation(summary = "거래 내역 조회", description = "특정 사용자의 거래 내역을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "거래 내역 조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    public CustomResponse<List<TransactionsResponse>> getTransactionsByUserId(
            @RequestBody TransactionsRequest transactionsRequest) {
        List<TransactionsResponse> transactions = transactionService.getTransactionsByUserId(transactionsRequest);
        return new CustomResponse<>(HttpStatus.OK, "사용자 거래 내역 조회 성공", transactions);
    }

    /**
     * 사용자의 자산 상세 정보를 조회
     *
     * @param userAssetRequest 사용자 자산 요청 데이터
     * @return 자산 정보 조회 결과
     */
    @PostMapping("/user/assets")
    @Operation(summary = "자산 정보 조회", description = "특정 사용자의 자산 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "자산 정보 조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    public CustomResponse<UserAssetResponse> getUserAssetDetails(@RequestBody UserAssetRequest userAssetRequest) {
        UserAssetResponse userAssetResponse = transactionService.findUserAssetDetails(userAssetRequest.getUserId());
        return new CustomResponse<>(HttpStatus.OK, "사용자 자산 정보 조회 성공", userAssetResponse);
    }

    /**
     * 사용자의 매수 가능한 자금을 조회
     *
     * @param availableBuyFundsRequest 매수 가능한 자금 요청 데이터
     * @return 매수 가능 자금 조회 결과
     */
    @PostMapping("/user/available-funds")
    @Operation(summary = "매수 가능 자금 조회", description = "특정 사용자의 매수 가능한 자금을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "매수 가능 자금 조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    public CustomResponse<AvailableBuyFundsResponse> getAvailableFunds(@RequestBody AvailableBuyFundsRequest availableBuyFundsRequest) {
        AvailableBuyFundsResponse response = transactionService.findAvailableFunds(availableBuyFundsRequest);
        return new CustomResponse<>(HttpStatus.OK, "사용자 매수 가능 자금 조회 성공", response);
    }

    /**
     * 사용자의 매도 가능한 코인 수량을 조회
     *
     * @param availableSellCoinsRequest 매도 가능 코인 요청 데이터
     * @return 매도 가능 코인 수량 조회 결과
     */
    @PostMapping("/user/available-coins")
    @Operation(summary = "매도 가능 코인 수량 조회", description = "특정 사용자의 매도 가능한 코인 수량을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "매도 가능 코인 수량 조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    public CustomResponse<AvailableSellCoinsResponse> getAvailableCoins(
            @RequestBody AvailableSellCoinsRequest availableSellCoinsRequest) {
        AvailableSellCoinsResponse response = transactionService.findAvailableCoins(availableSellCoinsRequest);
        return new CustomResponse<>(HttpStatus.OK, "사용자 매도 가능 코인 수량 조회 성공", response);
    }
}
