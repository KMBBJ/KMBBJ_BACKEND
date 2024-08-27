package com.kmbbj.backend.games.repository;

import com.kmbbj.backend.games.entity.Game;
import com.kmbbj.backend.games.enums.GameStatus;
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

}
