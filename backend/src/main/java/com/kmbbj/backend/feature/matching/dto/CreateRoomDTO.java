package com.kmbbj.backend.feature.matching.dto;

import com.kmbbj.backend.feature.matching.entity.StartSeedMoney;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
@Builder
public class CreateRoomDTO {
    // 방 제목
    private String title;

    // 시작 시드머니
    private StartSeedMoney startSeedMoney;

    // 마지막 라운드번호
    private int end;

    // 만든 날짜
    private LocalDateTime createDate;

    // 삭제 여부
    private boolean isDeleted;

    // 시작 여부
    private boolean isStarted;

    // 시작하기 전 딜레이 시간
    private int delay;
}
