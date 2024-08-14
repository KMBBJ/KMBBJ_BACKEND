package com.kmbbj.backend.games.repository;

import com.kmbbj.backend.games.entity.Game;
import com.kmbbj.backend.games.entity.Round;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoundRepository extends JpaRepository<Round, Long> {
    Optional<Round> findFirstByGameOrderByRoundNumberDesc(Game game); // 최신 라운드 조회
    boolean existsByGameAndRoundNumber(Game game, int roundNumber); // 라운드 번호 존재 확인여부
}
