package com.kmbbj.backend.feature.exchange.entity;

//거래의 상태를 지정하는 enum
public enum TransactionStatus {
    PENDING,     // 거래 대기
    COMPLETED,   // 거래 완료
    CANCELLED    // 거래 취소
}
