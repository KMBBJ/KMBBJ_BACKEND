package com.kmbbj.backend.matching.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RoomListDTO {
    private Long roomId;

    private String title;

    private int startSeedMoney;

    private int end;

    private LocalDateTime createDate;

    private int delay;

    private int userCount;
}
