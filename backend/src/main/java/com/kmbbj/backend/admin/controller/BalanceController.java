package com.kmbbj.backend.admin.controller;

import com.kmbbj.backend.admin.dto.UserBalanceAndTransactionsResponse;
import com.kmbbj.backend.admin.service.BalanceService;
import com.kmbbj.backend.balance.entity.AssetTransaction;
import com.kmbbj.backend.balance.entity.TotalBalance;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class BalanceController {

    private final BalanceService balanceService;

    public BalanceController(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    // 특정 사용자의 자산 정보와 거래 내역을 반환하는 엔드포인트
    @GetMapping("/balance/{userId}")
    public ResponseEntity<?> getUserBalanceAndTransactions(@PathVariable Long userId) {
        try {
            TotalBalance totalBalance = balanceService.getTotalBalanceByUserId(userId);
            List<AssetTransaction> transactions = balanceService.getAssetTransactionsByUserId(userId);

            UserBalanceAndTransactionsResponse userBalanceAndTransactionsResponse = new UserBalanceAndTransactionsResponse(totalBalance, transactions);

            return ResponseEntity.ok(userBalanceAndTransactionsResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}