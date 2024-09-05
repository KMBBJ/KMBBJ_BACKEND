package com.kmbbj.backend.global.config.jwt.filter;

import com.kmbbj.backend.auth.entity.Authority;
import com.kmbbj.backend.auth.entity.User;
import com.kmbbj.backend.auth.repository.UserRepository;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import com.kmbbj.backend.global.config.jwt.entity.redisToken;
import com.kmbbj.backend.global.config.jwt.infrastructure.CustomUserDetails;
import com.kmbbj.backend.global.config.jwt.infrastructure.JwtAuthenticationToken;
import com.kmbbj.backend.global.config.jwt.service.TokenService;
import com.kmbbj.backend.global.config.jwt.util.JwtTokenizer;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * JWT 토큰 인증 필터 클래스
 * 각 요청마다 JWT 토큰을 검증하고 인증을 설정
 */
@Slf4j
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenizer jwtTokenizer;
    private final TokenService tokenService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = getToken(request); // 요청에서 토큰을 추출
        if (StringUtils.hasText(token)) {
            try {
                // 토큰을 사용하여 인증 설정
                getAuthentication(token);

                // access 토큰 사용시마다 access, refresh 재발급
                makeNewResponseTokens(response, token);
            } catch (ExpiredJwtException e) { // 토큰 만료 시
                throw new ApiException(ExceptionEnum.EXPIRED_TOKEN);
            } catch (UnsupportedJwtException e) { // 지원하지 않는 토큰 사용 시
                throw new ApiException(ExceptionEnum.UNSUPPORTED_TOKEN);
            } catch (MalformedJwtException e) { // 유효하지 않은 토큰 사용 시
                throw new ApiException(ExceptionEnum.INVALID_TOKEN);
            } catch (IllegalArgumentException e) { // 올바르지 않은 파라미터 전달 시
                throw new ApiException(ExceptionEnum.TOKEN_NOT_FOUND);
            }
        }
        filterChain.doFilter(request, response); // 다음 필터로 요청을 전달
    }

    /**
     * 토큰을 사용하여 인증 설정
     *
     * @param token JWT 토큰
     */
    private void getAuthentication(String token) {
        Claims claims = jwtTokenizer.parseAccessToken(token); // 토큰에서 클레임을 파싱
        String email = claims.getSubject(); // 이메일을 가져옴
        Long userId = claims.get("userId", Long.class); // 사용자 ID를 가져옴
        Authority authority = Authority.valueOf(claims.get("authority", String.class)); // 사용자 권한을 가져옴

        Collection<? extends GrantedAuthority> authorities = Collections.singletonList(authority);

        CustomUserDetails userDetails = new CustomUserDetails(email, "", userId, (List<GrantedAuthority>) authorities);
        Authentication authentication = new JwtAuthenticationToken(authorities, userDetails, null); // 인증 객체 생성
        SecurityContextHolder.getContext().setAuthentication(authentication); // SecurityContextHolder에 인증 객체 설정
    }

    /**
     * 요청의 쿠키에서 토큰을 추출
     *
     * @param request 요청 객체
     * @return JWT 토큰
     */
    private String getToken(HttpServletRequest request) {
        return request.getHeader("Access-Token");
    }

    /**
     * access 토큰을 사용할 때마다 새로운 refresh 및 access 토큰을 만들어주는 메서드
     * redis에 새로운 refresh 토큰을 넣음
     *
     * @param response 응답
     * @param token 사용할 과거 access 토큰
     */
    private void makeNewResponseTokens(HttpServletResponse response, String token) throws IOException {
        Claims claims = jwtTokenizer.parseAccessToken(token);
        Long userId = claims.get("userId", Long.class);
        String email = claims.get("email", String.class);
        String nickname = claims.get("nickname", String.class);
        Authority authority = Authority.valueOf(claims.get("authority", String.class));

        // 유저의 계정 상태를 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ExceptionEnum.USER_NOT_FOUND));

        // 유저 테이블의 Suspended 값이 null이 아니라면(정지) 예외처리
        if (user.isSuspended()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "User is suspended.");
        }

        String newAccessToken = jwtTokenizer.createAccessToken(userId, email, nickname, authority);
        String newRefreshToken = jwtTokenizer.createRefreshToken(userId, email, nickname, authority);

        // 새로운 리프레시 토큰을 데이터베이스에 저장
        tokenService.saveOrRefresh(new redisToken(userId, newRefreshToken, tokenService.calculateTimeout()));

        // 새로운 리프레시 토큰을 응답 헤더에 추가
        response.setHeader("Access-Token", newAccessToken);
        response.setHeader("Refresh-Token", newRefreshToken);
    }
}