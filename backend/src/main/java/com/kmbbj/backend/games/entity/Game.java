package com.kmbbj.backend.games.entity;


import com.kmbbj.backend.games.enums.GameStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** Game 엔티티
 *  게임 세션
 */
@Entity
@Table (name = "games")
@NoArgsConstructor
@Getter@Setter
public class Game {

    // 게임 ID (기본 키)
    @Id @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column (name = "game_id")
    private Long gameId;

    /**
     * 게임의 현재 상태
     * ACTIVE 또는 COMPLETED 값 가질수 있음
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "enum")
    private GameStatus gameStatus;

    /**
     *  게임에 속한 라운드 목록
     *  게임과 라운드는 일대다 관계
     */
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Round> rounds = new ArrayList<>();

}
