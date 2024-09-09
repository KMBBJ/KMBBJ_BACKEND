package com.kmbbj.backend.feature.games.repository;

import com.kmbbj.backend.feature.games.entity.GameResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GameResultRepository extends JpaRepository<GameResult, Long> {
    List<GameResult> findByGame_GameId(UUID gameId);
}
