package com.kmbbj.backend.feature.exchange.service;

import com.kmbbj.backend.feature.exchange.service.cansel.CanselOrder;
import com.kmbbj.backend.feature.exchange.service.buy.save.SaveBuyOrder;
import com.kmbbj.backend.feature.exchange.service.execution.matching.ExecutionAllMatchingOrder;
import com.kmbbj.backend.feature.exchange.service.sell.save.SaveSellOrder;
import com.kmbbj.backend.feature.exchange.service.transaction.findlist.FindTransactionsByUserId;
import com.kmbbj.backend.feature.exchange.service.transaction.finduserassetdetails.FindUserAssetDetails;
import org.springframework.stereotype.Service;

@Service
public interface TransactionService extends
        //Sell
        SaveSellOrder,
        //Buy
        SaveBuyOrder,
        //Cansel
        CanselOrder,
        //excution
        ExecutionAllMatchingOrder,
        //transaction
        FindTransactionsByUserId,
        FindUserAssetDetails
{}