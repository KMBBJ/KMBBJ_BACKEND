package com.kmbbj.backend.global.config.jwt.filter;

import com.kmbbj.backend.auth.entity.Authority;
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
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
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
 */
@Slf4j
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenizer jwtTokenizer;
    private final TokenService tokenService;

    /**
     * 필터 메서드
     * 각 요청마다 JWT 토큰을 검증하고 인증을 설정
     *
     * @param request     요청 객체
     * @param response    응답 객체
     * @param filterChain 필터 체인
     * @throws ServletException 서블릿 예외
     * @throws IOException      입출력 예외
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = getToken(request); // 요청에서 토큰을 추출
        if (StringUtils.hasText(token)) {
            try {
                // 토큰을 사용하여 인증 설정
                getAuthentication(token);

                // access 토큰 사용시마다 access, refresh재발급
                makeNewResponseTokens(response, token);
            } catch (ExpiredJwtException e) { // 토큰 만료 시
                request.setAttribute("exception", "EXPIRED_TOKEN");
                log.error("Expired Token : {}", token, e);
                throw new BadCredentialsException("Expired token exception", e);
            } catch (UnsupportedJwtException e) { // 지원하지 않는 토큰 사용 시
                request.setAttribute("exception", "UNSUPPORTED_TOKEN");
                log.error("Unsupported Token: {}", token, e);
                throw new BadCredentialsException("Unsupported token exception", e);
            } catch (MalformedJwtException e) { // 유효하지 않은 토큰 사용 시
                request.setAttribute("exception", "INVALID_TOKEN");
                log.error("Invalid Token: {}", token, e);
                throw new BadCredentialsException("Invalid token exception", e);
            } catch (IllegalArgumentException e) { // 올바르지 않은 파라미터 전달 시
                request.setAttribute("exception", "NOT_FOUND_TOKEN");
                log.error("Token not found: {}", token, e);
                throw new BadCredentialsException("Token not found exception", e);
            } catch (Exception e) { // 알 수 없는 예외 발생 시
                request.setAttribute("exception", "NOT_FOUND_TOKEN");
                log.error("JWT Filter - Internal Error: {}", token, e);
                throw new BadCredentialsException("JWT filter internal exception", e);
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
     * 요청의 header의 Authriztion에서 토큰을 추출
     *
     * @param request 요청 객체
     * @return JWT 토큰
     */
    private String getToken(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("Access-Token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * access토큰을 사용할때마다 새로운 refresh랑 access를 만들어주는 코드
     * redisdp 새로운 refesh를 넣어준다.
     *
     * @param response 응답
     * @param token 사용할 과거 refresh토큰
     */
    private void makeNewResponseTokens(HttpServletResponse response, String token) {
        Claims claims = jwtTokenizer.parseAccessToken(token);
        Long userId = claims.get("userId", Long.class);
        String email = claims.get("email", String.class);
        String nickname = claims.get("nickname", String.class);
        Authority authority = Authority.valueOf(claims.get("authority", String.class));

        String newAccessToken = jwtTokenizer.createAccessToken(userId, email, nickname, authority);
        String newRefreshToken = jwtTokenizer.createRefreshToken(userId, email, nickname, authority);

        // 새로운 리프레시 토큰을 데이터베이스에 저장
        tokenService.saveOrRefresh(new redisToken(userId, newRefreshToken, tokenService.calculateTimeout()));

        // 새로운 액세스 토큰을 쿠키에 추가
        Cookie accessTokenCookie = new Cookie("Access-Token", newAccessToken);
        accessTokenCookie.setPath("/");      // 모든 경로에서 유효
        accessTokenCookie.setMaxAge((int) jwtTokenizer.getAccessTokenExpire()); // 액세스 토큰 만료 시간 설정
        response.addCookie(accessTokenCookie);

        // 새로운 리프레시 토큰을 응답 헤더에 추가
        response.setHeader("Refresh-Token", newRefreshToken);
    }
}