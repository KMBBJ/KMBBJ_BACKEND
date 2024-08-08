package com.kmbbj.backend.balance.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

//자산 변동 유형
@Getter
@AllArgsConstructor
public enum ChangeType {
    GAME,
    BONUS
}
