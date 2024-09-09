package com.kmbbj.backend.feature.matching.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
@Setter
public class RoomUserListDTO {
    // 닉네임
    private String userName;

    // 유저 자산
    private Long userAsset;

    // 유저 순위
//    private int userRank;

    // 방장 여부
    private boolean isManager;

}
