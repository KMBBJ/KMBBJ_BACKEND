package com.kmbbj.backend.feature.matching.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SortConditionDTO {
    // 초깃값 false
    private boolean isDeleted = false;

    // 초깃값 false
    private boolean isStarted = false;

    // 초깃값 0
    private int page = 0;

    // 초깃값 room_id
    private String sortField = "roomId";

    // 초깃값 DESC
    private String sortDirection = "DESC";
}
