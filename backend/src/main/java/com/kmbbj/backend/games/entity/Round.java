package com.kmbbj.backend.games.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table (name = "rounds")
@NoArgsConstructor
@Getter@Setter
public class Round {

    @Id @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column (name = "round_id", nullable = false)
    private Long roundId;

    @Column(name = "round_number")
    private Integer roundNumber;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private Game game;

    @OneToOne(mappedBy = "round", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private RoundResult roundResult;


}
