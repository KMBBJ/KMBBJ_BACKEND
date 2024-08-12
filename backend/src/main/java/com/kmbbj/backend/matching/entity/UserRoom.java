package com.kmbbj.backend.matching.entity;

import com.kmbbj.backend.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_rooms")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_room_id", nullable = false)
    private Long userRoomId;

    @Column(name = "is_manager",nullable = false)
    private Boolean isManager;

    @Column(name = "is_played", nullable = false)
    private Boolean isPlayed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

}
