package com.kmbbj.backend.balance.controller;

import com.kmbbj.backend.balance.entity.ChangeType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.time.LocalDateTime;

@Schema(description = "변동내역 응답용 Dto")
@ToString
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetTransactionresponse {
    @Schema(description = "거래 내역 id")
    private Long assetTransactionid;

    @Column(name = "change_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ChangeType changeType;

    @Schema(description = "변동 금액")
    private Long changeAmount;

    @Schema(description = "자산 변동 시간")
    private LocalDateTime changeDate;
}