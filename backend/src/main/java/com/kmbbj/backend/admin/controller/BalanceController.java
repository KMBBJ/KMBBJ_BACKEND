package com.kmbbj.backend.admin.controller;

import com.kmbbj.backend.admin.dto.UserBalanceAndTransactionsResponse;
import com.kmbbj.backend.admin.service.BalanceService;
import com.kmbbj.backend.balance.entity.AssetTransaction;
import com.kmbbj.backend.balance.entity.TotalBalance;

import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import com.kmbbj.backend.global.config.reponse.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class BalanceController {

    private final BalanceService balanceService;


    /**
     * 특정 사용자의 자산 정보와 거래 내역을 반환하는 엔드포인트
     *
     * @param userId 유저 id
     * @param page   첫 페이지, 기본값 0
     * @param size   페이지당 거래 내역 수, 기본값 10
     * @return 자산 정보와 거래 내역 조회 결과
     */
    @GetMapping("/{userId}")
    @Operation(summary = "특정 사용자의 자산 정보 및 거래 내역 조회", description = "특정 사용자의 자산 정보와 거래 내역을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "자산 정보 및 거래 내역 조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 유저를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    public CustomResponse<Map<String, Object>> getUserBalanceAndTransactions(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Map<String, Object> result = new HashMap<>();

        try {
            Pageable pageable = PageRequest.of(page, size);
            TotalBalance totalBalance = balanceService.getTotalBalanceByUserId(userId); // 해당 아이디의 총 자산
            Page<AssetTransaction> transactions = balanceService.getAssetTransactionsByUserId(userId, pageable); // 해당 아이디의 자산 변동 내역

            result.put("totalBalance", totalBalance); // 총 자산
            result.put("transactions", transactions.getContent()); // 변동 내역

            return new CustomResponse<>(HttpStatus.OK, "자산 정보 및 거래 내역 조회 성공", result);
        } catch (UsernameNotFoundException e) {
            throw new ApiException(ExceptionEnum.USER_NOT_FOUND); // 유저를 찾지 못한 경우
        } catch (Exception e) {
            throw new ApiException(ExceptionEnum.INTERNAL_SERVER_ERROR); // 서버 오류
        }
    }
}