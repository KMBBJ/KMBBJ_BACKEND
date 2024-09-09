package com.kmbbj.backend.auth.controller;

import com.kmbbj.backend.feature.auth.controller.UserController;
import com.kmbbj.backend.feature.auth.entity.Authority;
import com.kmbbj.backend.global.config.jwt.service.TokenService;
import com.kmbbj.backend.global.config.reponse.CustomResponse;
import com.kmbbj.backend.global.config.security.FindUserBySecurity;
import com.kmbbj.backend.feature.auth.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class UserControllerLogoutTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private FindUserBySecurity findUserBySecurity;

    @InjectMocks
    private UserController userController;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * 리프레시 토큰이 존재할 때 로그아웃을 테스트.
     */
    @Test
    void logoutSuccessTest() {
        User mockUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .nickname("nickname")
                .password("password")
                .authority(Authority.ROLE_USER)
                .build();
        // 필요한 필드 설정
        when(findUserBySecurity.getCurrentUser()).thenReturn(mockUser);

        // logout 메서드 호출
        CustomResponse<String> customResponse = userController.logout(request, response);

        // 검증
        assertEquals(HttpStatus.OK, customResponse.getStatus());
        assertEquals("로그아웃 되었습니다.", customResponse.getData());
        assertEquals("로그아웃 성공", customResponse.getMessage());

        verify(tokenService).invalidateRefreshToken(mockUser.getId());
    }

    /**
     * 리프레시 토큰이 존재하지 않을 때 로그아웃을 테스트
     */
    @Test
    void logoutNoRefreshTokenTest() {
        User mockUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .nickname("nickname")
                .password("password")
                .authority(Authority.ROLE_USER)
                .build();

        when(findUserBySecurity.getCurrentUser()).thenReturn(mockUser);

        // logout 메서드 호출
        CustomResponse<String> customResponse = userController.logout(request, response);

        // 검증
        assertEquals(HttpStatus.OK, customResponse.getStatus());
        assertEquals("로그아웃 되었습니다.", customResponse.getData());
        assertEquals("로그아웃 성공", customResponse.getMessage());

        verify(tokenService).invalidateRefreshToken(mockUser.getId());
    }
}