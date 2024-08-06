package com.kmbbj.backend.auth.controller;

import com.kmbbj.backend.global.config.jwt.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class UserControllerLogoutTest {

    @Mock
    private TokenService tokenService;

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
        // 요청에서 리프레시 토큰을 반환하도록 Mock 설정
        when(request.getHeader("Refresh-Token")).thenReturn("refreshToken");

        // logout 메서드 호출
        ResponseEntity<String> responseEntity = userController.logout(request, response);

        // 검증
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("로그아웃 되었습니다.", responseEntity.getBody());

        // 상호작용 검증
        verify(response).addCookie(any(Cookie.class));
        verify(tokenService).invalidateRefreshToken("refreshToken");
    }

    /**
     * 리프레시 토큰이 존재하지 않을 때 로그아웃을 테스트
     */
    @Test
    void logoutNoRefreshTokenTest() {
        // 요청에서 리프레시 토큰이 null을 반환하도록 Mock 설정
        when(request.getHeader("Refresh-Token")).thenReturn(null);

        // logout 메서드 호출
        ResponseEntity<String> responseEntity = userController.logout(request, response);

        // 검증
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("로그아웃 되었습니다.", responseEntity.getBody());

        // 상호작용 검증
        verify(response).addCookie(any(Cookie.class));
        verify(tokenService, never()).invalidateRefreshToken(anyString());
    }
}
