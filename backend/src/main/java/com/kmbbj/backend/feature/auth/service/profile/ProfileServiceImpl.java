package com.kmbbj.backend.feature.auth.service.profile;

import com.kmbbj.backend.feature.auth.controller.response.UserProfileReponse;
import com.kmbbj.backend.feature.auth.entity.User;
import com.kmbbj.backend.feature.auth.repository.UserRepository;
import com.kmbbj.backend.feature.balance.controller.AssetTransactionresponse;
import com.kmbbj.backend.feature.balance.entity.AssetTransaction;
import com.kmbbj.backend.feature.balance.entity.TotalBalance;
import com.kmbbj.backend.feature.balance.repository.totalbalances.TotalBalancesRepository;
import com.kmbbj.backend.feature.balance.repository.transaction.AssetTransactionRepository;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {
    private final UserRepository userRepository;
    private final TotalBalancesRepository totalBalancesRepository;
    private final AssetTransactionRepository assetTransactionRepository;

    @Override
    @Transactional
    public UserProfileReponse UserProfilefindByUserId(Long userId) {
        TotalBalance totalBalance = totalBalancesRepository.findByUserId(userId).orElseThrow(() -> new ApiException(ExceptionEnum.BALANCE_NOT_FOUND));
        List<AssetTransactionresponse> listAssetTransactionresponse = listAssetTranscationsToResponse(assetTransactionRepository.findAllByTotalBalance_TotalBalanceId(totalBalance.getTotalBalanceId()));
        User user = userRepository.findById(userId).orElseThrow(() -> new ApiException(ExceptionEnum.NOT_FOUND_USER));


        return UserProfileReponse.builder()
                .nickName(user.getNickname())
                .email(user.getEmail())
                .asset(totalBalance.getAsset())
                .assetTransactionList(listAssetTransactionresponse)
                .build();
    }

    private List<AssetTransactionresponse> listAssetTranscationsToResponse(List<AssetTransaction> listAssetTranscation) {
        // 리스트를 스트림으로 변환하여 각각의 AssetTransaction을 AssetTransactionresponse로 변환
        return listAssetTranscation.stream()
                .map(transaction -> AssetTransactionresponse.builder()
                        .assetTransactionid(transaction.getAssetTransactionId())
                        .changeType(transaction.getChangeType())
                        .changeAmount(transaction.getChangeAmount())
                        .changeDate(transaction.getCreateTime())
                        .build())
                .toList();
    }
}