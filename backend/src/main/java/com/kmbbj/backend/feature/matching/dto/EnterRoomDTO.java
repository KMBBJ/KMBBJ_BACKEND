package com.kmbbj.backend.feature.matching.dto;

import com.kmbbj.backend.feature.matching.entity.StartSeedMoney;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Builder
@Setter
public class EnterRoomDTO {
    // 현재 방 제목
    private String roomTitle;

    // 현재 방 인원수
    private int userCount;

    // 현재 방 평균 자산
    private Long averageAsset;

    // 현재 방 시드 머니
    private StartSeedMoney startSeedMoney;

    // 현재 방 딜레이
    private Integer delay;

    // 현재 방 라운드
    private Integer end;

    // 현재 방에 있는 유저 정보
    private List<RoomUserListDTO> roomUser;
}
