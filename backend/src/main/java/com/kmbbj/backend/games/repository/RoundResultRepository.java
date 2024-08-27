package com.kmbbj.backend.games.repository;

import com.kmbbj.backend.games.entity.RoundResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoundResultRepository extends JpaRepository<RoundResult, Long> {

    // 게임 ID로 라운드 결과 모두 조회
    List<RoundResult> findByRoundGameGameIdOrderByRoundRoundIdAsc(UUID gameId);

    // 라운드 ID 통해 해당 라운드 결과 조회
    Optional<RoundResult> findByRound_RoundId(Long roundId);

}
