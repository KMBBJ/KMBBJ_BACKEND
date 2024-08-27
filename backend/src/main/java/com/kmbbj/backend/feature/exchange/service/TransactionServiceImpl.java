package com.kmbbj.backend.feature.exchange.service;

import com.kmbbj.backend.feature.exchange.controller.request.*;
import com.kmbbj.backend.feature.exchange.controller.response.AvailableBuyFundsResponse;
import com.kmbbj.backend.feature.exchange.controller.response.AvailableSellCoinsResponse;
import com.kmbbj.backend.feature.exchange.controller.response.TransactionsResponse;
import com.kmbbj.backend.feature.exchange.controller.response.UserAssetResponse;
import com.kmbbj.backend.feature.exchange.service.buy.availablefunds.FindAvailableFundsImpl;
import com.kmbbj.backend.feature.exchange.service.buy.save.SaveBuyOrder;
import com.kmbbj.backend.feature.exchange.service.cansel.CanselOrder;
import com.kmbbj.backend.feature.exchange.service.execution.matching.ExecutionAllMatchingOrder;
import com.kmbbj.backend.feature.exchange.service.sell.availablecoinds.FindAvailableCoins;
import com.kmbbj.backend.feature.exchange.service.sell.save.SaveSellOrder;
import com.kmbbj.backend.feature.exchange.service.transaction.findlist.FindTransactionsByUserId;
import com.kmbbj.backend.feature.exchange.service.transaction.finduserassetdetails.FindUserAssetDetails;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {
    private final SaveSellOrder saveSellOrder;
    private final SaveBuyOrder saveBuyOrder;
    private final CanselOrder canselOrder;
    private final ExecutionAllMatchingOrder executionAllMatchingOrder;
    private final FindTransactionsByUserId findTransactionsByUserId;
    private final FindUserAssetDetails findUserAssetDetails;
    private final FindAvailableFundsImpl findAvailableFunds;
    private final FindAvailableCoins findAvailableCoins;

    public TransactionServiceImpl(@Qualifier("saveSellOrderImpl") SaveSellOrder saveSellOrder,
                                  @Qualifier("saveBuyOrderImpl") SaveBuyOrder saveBuyOrder,
                                  @Qualifier("findAvailableFundsImpl") FindAvailableFundsImpl findAvailableFundsImpl,
                                  @Qualifier("canselOrderImpl") CanselOrder canselOrder,
                                  @Qualifier("executionAllMatchingOrderImpl") ExecutionAllMatchingOrder executionAllMatchingOrder,
                                  @Qualifier("findTransactionsListByUserIdImpl") FindTransactionsByUserId findTransactionsByUserId,
                                  @Qualifier("findUserAssetDetailsImpl") FindUserAssetDetails findUserAssetDetails,
                                  @Qualifier("findAvailableCoinsImpl") FindAvailableCoins findAvailableCoins) {
        this.saveSellOrder = saveSellOrder;
        this.saveBuyOrder = saveBuyOrder;
        this.canselOrder = canselOrder;
        this.executionAllMatchingOrder = executionAllMatchingOrder;
        this.findTransactionsByUserId = findTransactionsByUserId;
        this.findUserAssetDetails = findUserAssetDetails;
        this.findAvailableFunds = findAvailableFundsImpl;
        this.findAvailableCoins = findAvailableCoins;
    }

    @Override
    public void saveBuyOrder(OrderRequest orderRequest) {
        saveBuyOrder.saveBuyOrder(orderRequest);
    }

    @Override
    public void saveSellOrder(OrderRequest orderRequest) {
        saveSellOrder.saveSellOrder(orderRequest);
    }

    @Override
    public void canselOrder(CanselRequest canselRequest) {
        canselOrder.canselOrder(canselRequest);
    }

    @Override
    public void matchOrders(Long coinId, BigDecimal price) {
        executionAllMatchingOrder.matchOrders(coinId, price);
    }

    @Override
    public List<TransactionsResponse> getTransactionsByUserId(TransactionsRequest transactionsRequest){
        return findTransactionsByUserId.getTransactionsByUserId(transactionsRequest);
    }

    @Override
    public UserAssetResponse FindUserAssetDetails(Long userId) {
        return findUserAssetDetails.FindUserAssetDetails(userId);
    }

    @Override
    public AvailableBuyFundsResponse findAvailableFunds(AvailableBuyFundsRequest request) {
        return findAvailableFunds.findAvailableFunds(request);
    }

    @Override
    public AvailableSellCoinsResponse findAvailableCoins(AvailableSellCoinsRequest request) {
        return findAvailableCoins.findAvailableCoins(request);
    }
}
