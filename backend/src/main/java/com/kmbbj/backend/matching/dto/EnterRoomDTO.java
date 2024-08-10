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
    private String roomTitle;
    private int userCount;
    private Long averageAsset;
    private List<RoomUserListDTO> roomUser;
}
