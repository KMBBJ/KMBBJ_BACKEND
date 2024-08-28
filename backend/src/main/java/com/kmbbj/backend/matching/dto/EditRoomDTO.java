package com.kmbbj.backend.matching.dto;

import com.kmbbj.backend.matching.entity.StartSeedMoney;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditRoomDTO {
    private String title;
    private int end;
    private StartSeedMoney startSeedMoney;
}
