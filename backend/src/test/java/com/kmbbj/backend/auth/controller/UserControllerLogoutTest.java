package com.kmbbj.backend.auth.controller;

import com.kmbbj.backend.global.config.jwt.service.TokenService;
import com.kmbbj.backend.global.config.reponse.CustomResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

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
        CustomResponse<String> customResponse = userController.logout(request, response);

        // 검증
        assertEquals(HttpStatus.OK, customResponse.getStatus());
        assertEquals("로그아웃 되었습니다.", customResponse.getData());
        assertEquals("로그아웃 성공", customResponse.getMessage());

        // 상호작용 검증
        verify(response).addCookie(any(Cookie.class));
    }

    /**
     * 리프레시 토큰이 존재하지 않을 때 로그아웃을 테스트
     */
    @Test
    void logoutNoRefreshTokenTest() {
        // 요청에서 리프레시 토큰이 null을 반환하도록 Mock 설정
        when(request.getHeader("Refresh-Token")).thenReturn(null);

        // logout 메서드 호출
        CustomResponse<String> customResponse = userController.logout(request, response);

        // 검증
        assertEquals(HttpStatus.OK, customResponse.getStatus());
        assertEquals("로그아웃 되었습니다.", customResponse.getData());
        assertEquals("로그아웃 성공", customResponse.getMessage());

        // 상호작용 검증
        verify(response).addCookie(any(Cookie.class));
    }
}