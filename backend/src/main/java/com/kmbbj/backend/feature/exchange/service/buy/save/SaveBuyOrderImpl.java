package com.kmbbj.backend.feature.exchange.service.buy.save;

import com.kmbbj.backend.feature.exchange.controller.request.OrderRequest;
import com.kmbbj.backend.feature.exchange.repository.cassandra.buy.BuyOrderRepository;
import com.kmbbj.backend.feature.exchange.util.ExchangeDTOMapper;
import com.kmbbj.backend.feature.exchange.util.SaveOrderUtil;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataAccessException;

@Service
@RequiredArgsConstructor
public class SaveBuyOrderImpl implements SaveBuyOrder {
    //카산드라 구매 기록 리파지토리
    private final BuyOrderRepository buyOrderRepository;
    //주문 생성 공통 로직을 불러올 유틸파일
    private final SaveOrderUtil saveOrderUtil;
    //DTO 관리 전용 Util
    private final ExchangeDTOMapper exchangeDTOMapper;

    /**
     * 주문 요청을 받아 BuyOrder를 저장하는 메서드.
     *
     * 이 메서드는 다음 작업을 수행함:
     * 1. OrderRequest를 검증하고, 이를 기반으로 Transaction을 생성한 뒤, 그 ID를 반환받음.
     * 2. 반환된 Transaction ID를 사용해 BuyOrder를 생성하고, Cassandra에 저장.
     * 3. 만약 데이터 저장 중 Cassandra 관련 예외가 발생하면, CASSANDRA_SAVE_EXCEPTION 예외를 발생시킴.
     *
     * @param orderRequest 주문 요청 객체
     * @throws ApiException Cassandra에 저장 중 예외가 발생할 경우 던짐
     */
    @Override
    @Transactional
    public void saveBuyOrder(OrderRequest orderRequest) {
        Long transactionId = saveOrderUtil.validateAndCreateTransaction(orderRequest);

        try {
            buyOrderRepository.save(
                    exchangeDTOMapper.orderRequestToBuyOrder(orderRequest, transactionId)
            );
        } catch (DataAccessException e) {
            throw new ApiException(ExceptionEnum.CASSANDRA_SAVE_EXCEPTION);
        }
    }
}