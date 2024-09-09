package com.kmbbj.backend.feature.games.repository;

import com.kmbbj.backend.feature.games.entity.Game;
import com.kmbbj.backend.feature.games.enums.GameStatus;
import com.kmbbj.backend.feature.matching.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Repository
public interface GameRepository extends JpaRepository<Game, UUID> {
    // 게임 ID 조회
    Optional<Game> findById(UUID gameId);

    // 모든 게임 상태 조회
    List<Game> findByGameStatus(GameStatus gameStatus);

    Game findActiveGameByRoom(Room room);
}
