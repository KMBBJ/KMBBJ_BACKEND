package com.kmbbj.backend.games.repository;

import com.kmbbj.backend.games.entity.GameBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameBalanceRepository extends JpaRepository<GameBalance, Long> {
    Optional<Long> findIdByUserId(Long userId);
    Optional<GameBalance> findByUserId(Long userId);
}