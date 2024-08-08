package com.kmbbj.backend.balance.repository.transaction;

import com.kmbbj.backend.balance.entity.AssetTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetTransactionRepository extends JpaRepository<AssetTransaction, Long>, CustomAssetTransactionRepository {
    Optional<AssetTransaction> findByAssetTransactionId(Long assetTransactionId);
    Optional<List<AssetTransaction>> findByTotalBalanceId(Long totalBalanceId);
}