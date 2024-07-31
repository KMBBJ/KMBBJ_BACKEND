package com.kmbbj.backend.matching.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class CreateRoomDTO {
    private String title;

    private int startSeedMoney;

    private int end;

    private LocalDateTime createDate;

    private boolean isDeleted;

    private boolean isStarted;

    private int delay;
}
