package com.kmbbj.backend.feature.admin.service;

import com.kmbbj.backend.feature.balance.entity.AssetTransaction;
import com.kmbbj.backend.feature.balance.entity.TotalBalance;
import com.kmbbj.backend.feature.balance.repository.totalbalances.TotalBalancesRepository;
import com.kmbbj.backend.feature.balance.repository.transaction.AssetTransactionRepository;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BalanceService {

    private final TotalBalancesRepository totalBalancesRepository;
    private final AssetTransactionRepository assetTransactionRepository;

    /**
     * 특정 유저의 총 자산 정보를 조회
     *
     * @param userId 유저 id
     * @return 유저의 총 자산 정보
     * @throws ApiException 유저의 자산 정보를 찾지 못한 경우 예외 발생
     */
    @Transactional(readOnly = true)
    public TotalBalance getTotalBalanceByUserId(Long userId) {
        return totalBalancesRepository.findByUserId(userId) // 유저 id를 통해 조회
                .orElseThrow(() -> new ApiException(ExceptionEnum.TOTAL_BALANCE_NOT_FOUND));
    }

    /**
     * 특정 유저의 자산 거래 내역을 페이지네이션하여 조회
     *
     * @param userId   유저 id
     * @param pageable 페이지 요청 정보 (페이지 번호, 크기, 정렬)
     * @return 유저의 자산 거래 내역 페이지
     * @throws ApiException 유저의 자산 정보를 찾지 못한 경우 예외 발생
     */
    @Transactional(readOnly = true)
    public Page<AssetTransaction> getAssetTransactionsByUserId(Long userId, Pageable pageable) {
        TotalBalance totalBalance = getTotalBalanceByUserId(userId); //유저 id를 통해 자산 조회

        // 정렬을 내림차순으로 설정
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Order.desc("createTime")));

        // 자산을 이용 거래 내역 조회
        return assetTransactionRepository.findAllByTotalBalance_TotalBalanceId(totalBalance.getTotalBalanceId(), sortedPageable);
    }
}
