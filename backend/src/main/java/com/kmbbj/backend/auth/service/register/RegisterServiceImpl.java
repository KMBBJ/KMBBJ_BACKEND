package com.kmbbj.backend.auth.service.register;

import com.kmbbj.backend.auth.controller.request.UserJoinRequest;
import com.kmbbj.backend.auth.entity.Authority;
import com.kmbbj.backend.auth.entity.User;
import com.kmbbj.backend.auth.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterServiceImpl implements RegisterService {
    private final UserRepository userRepostiory;
    private final PasswordEncoder passwordEncoder;

    /**
     * 사용자 등록 처리
     *
     * @param userJoinRequest 회원가입 요청 데이터
     */
    @Transactional
    @Override
    public void registerUser(UserJoinRequest userJoinRequest) {
        // 이메일 중복 확인
        if (userRepostiory.findByEmail(userJoinRequest.getEmail()).isPresent()) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        // 비밀번호 인코딩
        String encodedPassword = passwordEncoder.encode(userJoinRequest.getPassword());

        // User 엔티티로 변환 및 저장
        User user = User.builder()
                .email(userJoinRequest.getEmail())
                .password(encodedPassword)
                .authority(Authority.USER)
                .build();

        userRepostiory.save(user);
    }
}
