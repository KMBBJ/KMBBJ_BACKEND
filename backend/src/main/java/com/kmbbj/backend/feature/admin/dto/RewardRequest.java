package com.kmbbj.backend.feature.admin.dto;

import com.kmbbj.backend.feature.balance.entity.ChangeType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RewardRequest {
    private Long amount;
    private ChangeType changeType;
}
