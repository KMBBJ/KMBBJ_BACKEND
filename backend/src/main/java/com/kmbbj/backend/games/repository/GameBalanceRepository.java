package com.kmbbj.backend.games.repository;

import com.kmbbj.backend.auth.entity.User;
import com.kmbbj.backend.games.entity.Game;
import com.kmbbj.backend.games.entity.GameBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameBalanceRepository extends JpaRepository<GameBalance, Long> {
    Optional<Long> findIdByUserId(Long userId);

    // 게임에 참여한 사용자들의 게임 잔액 정보 조회
    List<GameBalance> findByGameAndUserIn(Game game, List<User> participants);

    Optional<GameBalance> findByUserId(Long userId);

    List<GameBalance> findByGame(Game game);

}