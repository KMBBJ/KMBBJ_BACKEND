package com.kmbbj.backend.feature.balance.repository.totalbalances;

import com.kmbbj.backend.feature.balance.entity.TotalBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TotalBalancesRepository extends JpaRepository<TotalBalance, Long>, CustomTotalBalancesRepository {
    Optional<TotalBalance> findByUserId(Long userid);
}