package com.kmbbj.backend.games.entity;


import com.kmbbj.backend.games.enums.GameStatus;
import com.kmbbj.backend.matching.entity.Room;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Game 엔티티
 *  게임 세션
 */
@Entity
@Table (name = "games")
@NoArgsConstructor
@Getter@Setter
public class Game {

    // 게임 ID (기본 키)
    @Id @GeneratedValue (generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column (name = "game_id", updatable = false,nullable = false)
    private UUID gameId;

    /**
     * 게임의 현재 상태
     * ACTIVE 또는 COMPLETED 값 가질수 있음
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "enum", nullable = false)
    private GameStatus gameStatus;

    /**
     *  게임에 속한 라운드 목록
     *  게임과 라운드는 일대다 관계
     */
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Round> rounds = new ArrayList<>();

    // 방과의 일대일 관계 설정
    @OneToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

}
