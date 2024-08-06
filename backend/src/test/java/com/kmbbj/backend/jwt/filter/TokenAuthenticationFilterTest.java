package com.kmbbj.backend.jwt.filter;

import com.kmbbj.backend.auth.entity.Authority;
import com.kmbbj.backend.global.config.jwt.entity.redisToken;
import com.kmbbj.backend.global.config.jwt.filter.TokenAuthenticationFilter;
import com.kmbbj.backend.global.config.jwt.service.TokenService;
import com.kmbbj.backend.global.config.jwt.util.JwtTokenizer;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.BadCredentialsException;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * TokenAuthenticationFilter의 단위 테스트를 위한 클래스
 */
class TokenAuthenticationFilterTest {

    @Mock
    private JwtTokenizer jwtTokenizer;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private TokenAuthenticationFilter tokenAuthenticationFilter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private Claims claims;

    /**
     * 각 테스트 전에 Mockito 모의 객체 초기화
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * 유효한 토큰을 사용하여 필터를 테스트
     */
    @Test
    void doFilterValidToken() throws ServletException, IOException {
        Cookie cookie = new Cookie("Access-Token", "validToken");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(jwtTokenizer.parseAccessToken("validToken")).thenReturn(claims);
        when(claims.get("userId", Long.class)).thenReturn(1L);
        when(claims.get("email", String.class)).thenReturn("test@example.com");
        when(claims.get("nickname", String.class)).thenReturn("nickname");
        when(claims.get("authority", String.class)).thenReturn("USER");

        when(jwtTokenizer.createAccessToken(anyLong(), anyString(), anyString(), any(Authority.class))).thenReturn("newAccessToken");
        when(jwtTokenizer.createRefreshToken(anyLong(), anyString(), anyString(), any(Authority.class))).thenReturn("newRefreshToken");
        when(tokenService.calculateTimeout()).thenReturn(LocalDateTime.now().plusHours(1));

        tokenAuthenticationFilter.doFilter(request, response, filterChain);

        verify(response).addCookie(any(Cookie.class));
        verify(response).setHeader("Refresh-Token", "newRefreshToken");
        verify(tokenService).saveOrRefresh(any(redisToken.class));
        verify(filterChain).doFilter(request, response);
    }

    /**
     * 만료된 토큰을 사용하여 필터를 테스트
     */
    @Test
    void doFilterExpiredToken() {
        Cookie cookie = new Cookie("Access-Token", "expiredToken");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        doThrow(new ExpiredJwtException(null, null, "Expired token")).when(jwtTokenizer).parseAccessToken("expiredToken");

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            tokenAuthenticationFilter.doFilter(request, response, filterChain);
        });

        assertEquals("Expired token exception", exception.getMessage());
    }

    /**
     * 지원되지 않는 토큰을 사용하여 필터를 테스트
     */
    @Test
    void doFilterUnsupportedToken() {
        Cookie cookie = new Cookie("Access-Token", "unsupportedToken");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        doThrow(new UnsupportedJwtException("Unsupported token")).when(jwtTokenizer).parseAccessToken("unsupportedToken");

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            tokenAuthenticationFilter.doFilter(request, response, filterChain);
        });

        assertEquals("Unsupported token exception", exception.getMessage());
    }

    /**
     * 유효하지 않은 토큰을 사용하여 필터를 테스트
     */
    @Test
    void doFilterInvalidToken() {
        Cookie cookie = new Cookie("Access-Token", "invalidToken");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        doThrow(new MalformedJwtException("Invalid token")).when(jwtTokenizer).parseAccessToken("invalidToken");

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            tokenAuthenticationFilter.doFilter(request, response, filterChain);
        });

        assertEquals("Invalid token exception", exception.getMessage());
    }

    /**
     * 토큰이 없는 경우 필터를 테스트
     */
    @Test
    void doFilterNoToken() throws ServletException, IOException {
        when(request.getCookies()).thenReturn(null);

        tokenAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }
}