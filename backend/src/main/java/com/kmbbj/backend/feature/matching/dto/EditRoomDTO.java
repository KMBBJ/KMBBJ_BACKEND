package com.kmbbj.backend.feature.matching.dto;

import com.kmbbj.backend.feature.matching.entity.StartSeedMoney;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditRoomDTO {
    private String title;
    private int end;
    private StartSeedMoney startSeedMoney;
}
