package com.kmbbj.backend.games.repository;

import com.kmbbj.backend.games.entity.RoundResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoundResultRepository extends JpaRepository<RoundResult, Long> {

}
