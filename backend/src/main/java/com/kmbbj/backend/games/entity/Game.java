package com.kmbbj.backend.games.entity;


import com.kmbbj.backend.games.enums.GameStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table (name = "games")
@NoArgsConstructor
@Getter@Setter
public class Game {

    @Id @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column (name = "game_id")
    private Long gameId;

    @Enumerated(EnumType.STRING)
    @Column(name = "enum")
    private GameStatus gameStatus;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Round> rounds = new ArrayList<>();

}
