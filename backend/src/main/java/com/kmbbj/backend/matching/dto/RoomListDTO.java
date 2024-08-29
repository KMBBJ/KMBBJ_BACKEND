package com.kmbbj.backend.matching.dto;

import com.kmbbj.backend.matching.entity.StartSeedMoney;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RoomListDTO {
    private Long roomId;

    private String title;

    private StartSeedMoney startSeedMoney;

    private int end;

    private LocalDateTime createDate;

    private int delay;

    private int userCount;
}
