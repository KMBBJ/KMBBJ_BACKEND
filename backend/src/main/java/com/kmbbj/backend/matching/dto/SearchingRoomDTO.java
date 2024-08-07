package com.kmbbj.backend.matching.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchingRoomDTO {
    // 찾는 방 제목
    private String title;

    // 페이지 번호
    private int page = 0;
}
