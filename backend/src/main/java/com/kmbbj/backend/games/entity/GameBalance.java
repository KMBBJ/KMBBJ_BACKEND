package com.kmbbj.backend.games.entity;

import com.kmbbj.backend.auth.entity.User;
import com.kmbbj.backend.matching.entity.Room;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * GameBalance 엔티티
 * 게임 내 사용자의 계좌 정보
 */
@Entity
@Table (name = "game_balances")
@Getter@Setter
@NoArgsConstructor
@AllArgsConstructor

public class GameBalance {

    // 게임 잔액 (기본 키)
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long gameBalancesId;

    /**
     * 게임 계좌 속한 룸 (게임 세션)
     * 다대일 관계 , 하나의 게임 여러 게임 계좌 존재
     */
    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    /**
     *  게임 계좌의 소유자인 사용자
     *  일대일 관계 , 각 게임 계좌 사용자와 연결 
     */
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    //게임내 사용자가 가지고 있는 돈
    @Column(name = "seed")
    private Long seed;
}
