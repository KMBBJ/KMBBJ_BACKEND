package com.kmbbj.backend.games.entity;

import com.kmbbj.backend.auth.entity.User;
import com.kmbbj.backend.matching.entity.Room;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table (name = "game_balances")
@Getter@Setter
@NoArgsConstructor
@AllArgsConstructor

public class GameBalance {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long gameBalancesId;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
