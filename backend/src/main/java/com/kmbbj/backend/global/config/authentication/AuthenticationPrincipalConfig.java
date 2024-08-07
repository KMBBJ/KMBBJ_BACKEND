package com.kmbbj.backend.global.config.authentication;

import com.kmbbj.backend.auth.entity.User;
import com.kmbbj.backend.auth.service.UserService;
import com.kmbbj.backend.global.config.jwt.infrastructure.CustomUserDetails;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationPrincipalConfig {
    private final UserService userService;

    public User getCurrentUser(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userService.UserfindById(userDetails.getUserId()).orElseThrow(() -> new RuntimeException("유저를 찾지 못했습니다"));
        return user;
    }
}
