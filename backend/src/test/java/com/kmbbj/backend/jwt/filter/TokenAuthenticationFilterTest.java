package com.kmbbj.backend.jwt.filter;

import com.kmbbj.backend.auth.entity.Authority;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import com.kmbbj.backend.global.config.jwt.entity.redisToken;
import com.kmbbj.backend.global.config.jwt.filter.TokenAuthenticationFilter;
import com.kmbbj.backend.global.config.jwt.service.TokenService;
import com.kmbbj.backend.global.config.jwt.util.JwtTokenizer;
import io.jsonwebtoken.Claims;
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

import java.io.IOException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

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

    @Test
    void doFilterExpiredToken() {
        Cookie cookie = new Cookie("Access-Token", "expiredToken");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        doThrow(new ApiException(ExceptionEnum.EXPIRED_TOKEN)).when(jwtTokenizer).parseAccessToken("expiredToken");

        ApiException exception = assertThrows(ApiException.class, () -> {
            tokenAuthenticationFilter.doFilter(request, response, filterChain);
        });

        assertEquals(ExceptionEnum.EXPIRED_TOKEN, exception.getException());
    }

    @Test
    void doFilterUnsupportedToken() {
        Cookie cookie = new Cookie("Access-Token", "unsupportedToken");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        doThrow(new ApiException(ExceptionEnum.UNSUPPORTED_TOKEN)).when(jwtTokenizer).parseAccessToken("unsupportedToken");

        ApiException exception = assertThrows(ApiException.class, () -> {
            tokenAuthenticationFilter.doFilter(request, response, filterChain);
        });

        assertEquals(ExceptionEnum.UNSUPPORTED_TOKEN, exception.getException());
    }

    @Test
    void doFilterInvalidToken() {
        Cookie cookie = new Cookie("Access-Token", "invalidToken");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        doThrow(new ApiException(ExceptionEnum.INVALID_TOKEN)).when(jwtTokenizer).parseAccessToken("invalidToken");

        ApiException exception = assertThrows(ApiException.class, () -> {
            tokenAuthenticationFilter.doFilter(request, response, filterChain);
        });

        assertEquals(ExceptionEnum.INVALID_TOKEN, exception.getException());
    }

    @Test
    void doFilterNoToken() throws ServletException, IOException {
        when(request.getCookies()).thenReturn(null);

        tokenAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }
}