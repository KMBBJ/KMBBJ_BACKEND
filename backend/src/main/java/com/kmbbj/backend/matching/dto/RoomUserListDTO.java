package com.kmbbj.backend.matching.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
@Setter
public class RoomUserListDTO {
    private String userName;
    private Long userAsset;
//    private int userRank;
    private boolean isManager;

}
