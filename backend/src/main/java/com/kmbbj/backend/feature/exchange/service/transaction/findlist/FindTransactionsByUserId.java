package com.kmbbj.backend.feature.exchange.service.transaction.findlist;

import com.kmbbj.backend.feature.exchange.controller.request.TransactionsRequest;
import com.kmbbj.backend.feature.exchange.controller.response.TransactionsResponse;

import java.util.List;

public interface FindTransactionsByUserId {
    List<TransactionsResponse> getTransactionsByUserId(TransactionsRequest transactionsRequest);
}