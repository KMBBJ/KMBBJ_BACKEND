package com.kmbbj.backend.feature.exchange.repository.postgre;

import com.kmbbj.backend.feature.exchange.entity.postgre.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}