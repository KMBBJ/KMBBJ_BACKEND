package com.kmbbj.backend.matching.dto;

import com.kmbbj.backend.auth.entity.User;
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

    // 현재 방에 있는 유저 정보
    private List<RoomUserListDTO> roomUser;
}
