package com.kmbbj.backend.jwt.Controller;

import com.kmbbj.backend.auth.entity.Authority;
import com.kmbbj.backend.global.config.jwt.controller.RefreshController;
import com.kmbbj.backend.global.config.jwt.entity.redisToken;
import com.kmbbj.backend.global.config.jwt.service.TokenService;
import com.kmbbj.backend.global.config.jwt.util.JwtTokenizer;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class RefreshControllerTest {

    @Mock
    private JwtTokenizer jwtTokenizer;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private RefreshController refreshController;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Claims claims;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * 리프레시 토큰이 존재하지 않을 때의 시나리오를 테스트합니다.
     */
    @Test
    void refreshTokensNoRefreshToken() {
        when(request.getHeader("Authorization")).thenReturn(null);

        ResponseEntity<?> responseEntity = refreshController.refreshTokens(request, response);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        assertEquals("리프레시 토큰이 없습니다.", responseEntity.getBody());
    }

    /**
     * 유효하지 않은 리프레시 토큰 시나리오를 테스트
     */
    @Test
    void refreshTokensInvalidRefreshToken() {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalidToken");
        doThrow(new RuntimeException("Invalid token")).when(jwtTokenizer).parseRefreshToken(anyString());

        ResponseEntity<?> responseEntity = refreshController.refreshTokens(request, response);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        assertEquals("유효하지 않은 리프레시 토큰입니다.", responseEntity.getBody());
    }

    /**
     * 리프레시 토큰이 유효한 경우 새로운 토큰을 발급하는 시나리오를 테스트
     */
    @Test
    void refreshTokensValidRefreshToken() {
        when(request.getHeader("Authorization")).thenReturn("Bearer validToken");
        when(jwtTokenizer.parseRefreshToken(anyString())).thenReturn(claims);
        when(claims.get("userId", Long.class)).thenReturn(1L);
        when(claims.get("email", String.class)).thenReturn("test@example.com");
        when(claims.get("nickname", String.class)).thenReturn("testNickname");
        when(claims.get("authority", String.class)).thenReturn("USER");

        when(jwtTokenizer.createAccessToken(anyLong(), anyString(), anyString(), any(Authority.class))).thenReturn("newAccessToken");
        when(jwtTokenizer.createRefreshToken(anyLong(), anyString(), anyString(), any(Authority.class))).thenReturn("newRefreshToken");
        when(tokenService.calculateTimeout()).thenReturn(LocalDateTime.now().plusHours(1));

        ResponseEntity<?> responseEntity = refreshController.refreshTokens(request, response);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(response).addCookie(any(Cookie.class));
        verify(response).setHeader("Refresh-Token", "newRefreshToken");
        verify(tokenService).saveOrRefresh(any(redisToken.class));
    }
}