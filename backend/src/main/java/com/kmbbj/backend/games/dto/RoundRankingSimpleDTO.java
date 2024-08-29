package com.kmbbj.backend.games.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter@Getter
@AllArgsConstructor
public class RoundRankingSimpleDTO {
    private String user;
    private int rank;
    private String profit;
    private String loss;
}
