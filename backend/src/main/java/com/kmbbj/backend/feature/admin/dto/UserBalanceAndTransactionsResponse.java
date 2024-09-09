package com.kmbbj.backend.feature.admin.dto;

import com.kmbbj.backend.feature.balance.entity.AssetTransaction;
import com.kmbbj.backend.feature.balance.entity.TotalBalance;
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
