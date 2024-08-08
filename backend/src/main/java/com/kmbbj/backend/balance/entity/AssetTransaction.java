package com.kmbbj.backend.balance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.format.annotation.DateTimeFormat;

import java.security.Timestamp;

//자산 변동 내역
@ToString
@Getter
@Builder
@RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "asset_transactions")
public class AssetTransaction {
    //변동 내역 id값
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private long assetTransactionId;

    //변동 유형
    @Column(name = "change_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ChangeType changeType;

    //변동 금액
    @Column(name = "change_amount", nullable = false)
    private Long changeAmount;

    //자산 변동 시간
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private Timestamp createTime;

    //변경된 계좌
    @JoinColumn(name = "total_balance_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private TotalBalance totalBalance;
}
