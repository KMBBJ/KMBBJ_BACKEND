package com.kmbbj.backend.feature.games.repository;

import com.kmbbj.backend.feature.games.entity.RoundResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoundResultRepository extends JpaRepository<RoundResult, Long> {

}
