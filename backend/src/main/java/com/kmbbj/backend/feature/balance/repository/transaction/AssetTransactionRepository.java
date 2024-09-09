package com.kmbbj.backend.feature.balance.repository.transaction;

import com.kmbbj.backend.feature.balance.entity.AssetTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetTransactionRepository extends JpaRepository<AssetTransaction, Long>, CustomAssetTransactionRepository {
    Optional<AssetTransaction> findByAssetTransactionId(Long assetTransactionId);
    List<AssetTransaction> findAllByTotalBalance_TotalBalanceId(Long totalBalanceId);
    Page<AssetTransaction> findAllByTotalBalance_TotalBalanceId(Long totalBalanceId, Pageable pageable);

}