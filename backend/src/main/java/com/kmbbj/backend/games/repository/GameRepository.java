package com.kmbbj.backend.games.repository;

import com.kmbbj.backend.games.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    // 게임 ID 조회
    Optional<Game> findById(Long gameId);
}
