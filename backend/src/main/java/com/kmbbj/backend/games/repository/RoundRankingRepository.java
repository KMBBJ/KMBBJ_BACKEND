package com.kmbbj.backend.games.repository;

import com.kmbbj.backend.games.entity.Round;
import com.kmbbj.backend.games.entity.RoundRanking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoundRankingRepository extends JpaRepository<RoundRanking, Long> {
    List<RoundRanking> findByRoundOrderByRankAsc(Round round);
}
