package com.kmbbj.backend.feature.admin.service;

import com.kmbbj.backend.feature.admin.entity.TokenBlacklist;
import com.kmbbj.backend.feature.admin.repository.TokenBlacklistRepository;
import com.kmbbj.backend.feature.auth.entity.User;
import com.kmbbj.backend.feature.auth.repository.UserRepository;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import com.kmbbj.backend.global.config.jwt.entity.redisToken;
import com.kmbbj.backend.global.config.jwt.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BlackListUserService {

    private final UserRepository userRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final TokenService tokenService;

    /**
     * 유저 계정을 정지시키고 해당 유저의 토큰을 블랙리스트에 추가합니다.
     *
     * @param userId            정지할 유저의 id
     * @param suspensionEndDate 정지 해제 날짜
     */
    @Transactional
    public void suspendUser(Long userId, LocalDateTime suspensionEndDate) {
        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ExceptionEnum.USER_NOT_FOUND));

        user.setSuspensionEndDate(suspensionEndDate); // 정지 종료 날짜 설정
        userRepository.save(user);

        redisToken redisToken = tokenService.getToken(userId);// 사용자 리프레시 토큰 가져오기

        // 토큰이 존재하는 경우에만 블랙리스트에 추가
        if (redisToken != null && redisToken.getTokenValue() != null && !redisToken.getTokenValue().isEmpty()) {
            TokenBlacklist blacklist = TokenBlacklist.builder()
                    .id(userId)
                    .token(redisToken.getTokenValue())
                    .expiryDate(suspensionEndDate)
                    .build();

            tokenBlacklistRepository.save(blacklist);
            System.out.println("블랙리스트 토큰 저장 완료: " + userId);
        } else {
            System.out.println("유저의 토큰을 찾지 못했습니다: " + userId + ", 블랙리스트에 추가하지 않습니다.");
        }
    }

    /**
     * 유저 계정의 정지를 해제
     *
     * @param userId 정지 해제할 유저의 ID
     */
    @Transactional
    public void unsuspendUser(Long userId) {
        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ExceptionEnum.USER_NOT_FOUND));

        // 정지 날짜 해제
        user.setSuspensionEndDate(null);
        userRepository.save(user);

        // 블랙리스트에서 해당 유저의 토큰 삭제
        tokenBlacklistRepository.deleteById(userId);
        System.out.println("해당 아이디의 토큰을 블랙리스트에서 삭제하였습니다: " + userId);
    }

    /**
     * 유저를 ID로 조회하고, 정지 기간이 만료되었으면 정지를 해제합니다.
     *
     * @param userId 조회할 유저의 ID
     * @return 조회된 유저의 Optional 객체
     */
    @Transactional
    public Optional<User> findById(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            throw new ApiException(ExceptionEnum.USER_NOT_FOUND);
        }

        User user = userOptional.get();

        // 정지 기간이 만료되었는지 확인
        if (user.isSuspended() && user.getSuspensionEndDate().isBefore(LocalDateTime.now())) {
            user.setSuspensionEndDate(null);
            userRepository.save(user);

            // 블랙리스트에서 해당 유저의 토큰 삭제
            tokenBlacklistRepository.deleteById(userId);
            System.out.println("정지 기간이 만료되어 유저의 정지를 해제하고 블랙리스트에서 토큰을 삭제하였습니다: " + userId);
        }

        return Optional.of(user);
    }
}
