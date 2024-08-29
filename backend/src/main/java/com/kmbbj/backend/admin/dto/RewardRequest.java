package com.kmbbj.backend.admin.dto;

import com.kmbbj.backend.balance.entity.ChangeType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RewardRequest {
    private Long amount;
    private ChangeType changeType;

    // Getters and Setters
}
