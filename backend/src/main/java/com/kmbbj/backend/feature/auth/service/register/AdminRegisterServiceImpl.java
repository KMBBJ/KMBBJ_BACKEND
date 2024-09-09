package com.kmbbj.backend.feature.auth.service.register;


import com.kmbbj.backend.feature.auth.controller.request.UserJoinRequest;
import com.kmbbj.backend.feature.auth.entity.Authority;
import com.kmbbj.backend.feature.auth.entity.User;
import com.kmbbj.backend.feature.auth.repository.UserRepository;
import com.kmbbj.backend.feature.auth.util.RandomNickname;
import com.kmbbj.backend.feature.balance.entity.TotalBalance;
import com.kmbbj.backend.feature.balance.service.BalanceService;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminRegisterServiceImpl implements AdminRegisterService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RandomNickname randomNickname;
    private final BalanceService balanceService;

    /**
     * 어드민 등록 처리
     *
     * @param userJoinRequest 회원가입 요청 데이터 (어드민 전용)
     */
    @Transactional
    @Override
    public void registerAdmin(UserJoinRequest userJoinRequest) {
        // 이메일 중복 확인
        if (userRepository.findByEmail(userJoinRequest.getEmail()).isPresent()) {
            throw new ApiException(ExceptionEnum.EXIST_EMAIL);
        }

        // 비밀번호 인코딩
        String encodedPassword = passwordEncoder.encode(userJoinRequest.getPassword());

        // User 엔티티로 변환 및 저장
        User admin = User.builder()
                .email(userJoinRequest.getEmail())
                .nickname(getNickname())
                .password(encodedPassword)
                .authority(Authority.ROLE_ADMIN)  // 어드민 권한 설정
                .isDeleted(false)
                .build();

        TotalBalance totalBalance = TotalBalance.builder()
                .asset(30000000L)
                .user(admin)
                .build();

        userRepository.save(admin);
        balanceService.makeTotalBalance(totalBalance);
    }

    /**
     * 중복되지 않은 랜덤 닉네임 생성
     *
     * @return unique random nickname
     */
    public String getNickname() {
        String nickname = randomNickname.generate();
        return nickname;
    }
}