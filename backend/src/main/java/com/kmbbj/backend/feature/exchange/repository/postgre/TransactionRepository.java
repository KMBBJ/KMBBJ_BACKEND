package com.kmbbj.backend.feature.exchange.repository.postgre;

import com.kmbbj.backend.feature.exchange.entity.postgre.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByGameId(UUID gameId);
}