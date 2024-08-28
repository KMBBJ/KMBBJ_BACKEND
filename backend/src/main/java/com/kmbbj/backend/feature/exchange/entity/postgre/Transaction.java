package com.kmbbj.backend.feature.exchange.entity.postgre;

import com.kmbbj.backend.feature.exchange.entity.TransactionStatus;
import com.kmbbj.backend.feature.exchange.entity.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class Transaction {

    @Id
    @Column(name = "transaction_id")
    private Long transactionId; // 거래 ID, 기본 키로 사용됨

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type")
    private TransactionType transactionType; // 거래 유형, enum 타입 (buy, sell)

    @Column(name = "quantity", precision = 20, scale = 10)
    private BigDecimal quantity; // 거래된 수량, 소수점 10자리까지 저장

    @Column(name = "price")
    private Long price; // 거래된 가격

    @Column(name = "total_price")
    private Long totalPrice; // 거래된 총 가격

    @Column(name = "create_date")
    private LocalDateTime createDate; // 생성일시, 거래가 생성된 시간

    @Setter
    @Column(name = "status")
    private TransactionStatus status = TransactionStatus.PENDING; // 체결이 됐는지 알려줌

    @Setter
    @Column(name = "execution_date", nullable = true)
    private LocalDateTime executionDate; // 거래 체결일, nullable 설정됨

    @Column(name = "balances_id")
    private Long balancesId; // 잔액 ID, 외래 키로 사용될 수 있음

    @Column(name = "game_id")
    private UUID gameId; // 게임 ID, 외래 키로 사용될 수 있음

    @Column(name = "coin_id")
    private Long coinId; // 코인 아이디
}