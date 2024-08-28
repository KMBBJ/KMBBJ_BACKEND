package com.kmbbj.backend.games.entity;

import com.kmbbj.backend.auth.entity.User;
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
    @Column(name = "game_balances_id")
    private Long gameBalancesId;

    /**
     * 하나의 게임에 여러 게임 계좌 존재
     * 게임 A 있다면 사용자 1 2 3 가 A 참여하면 게임 A 계좌 3개 생성
     */
    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    /**
     *  게임 계좌의 소유자인 사용자
     *  일대일 관계 , 각 게임 계좌 사용자와 연결
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 게임에서 사용자가 보유한 시드머니(자금)
     */
    @Column(name = "seed", nullable = false)

    private Long seed;
}
