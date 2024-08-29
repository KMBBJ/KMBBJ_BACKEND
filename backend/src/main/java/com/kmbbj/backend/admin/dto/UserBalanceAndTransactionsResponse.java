package com.kmbbj.backend.admin.dto;

import com.kmbbj.backend.balance.entity.AssetTransaction;
import com.kmbbj.backend.balance.entity.TotalBalance;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class UserBalanceAndTransactionsResponse {
    private final TotalBalance totalBalance;
    private final List<AssetTransaction> assetTransactions;
}
