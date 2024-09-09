package com.kmbbj.backend.feature.admin.token.filter;

import com.kmbbj.backend.feature.admin.repository.TokenBlacklistRepository;
import com.kmbbj.backend.feature.admin.service.BlackListUserService;
import com.kmbbj.backend.feature.auth.entity.User;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import com.kmbbj.backend.global.config.jwt.entity.redisToken;
import com.kmbbj.backend.global.config.jwt.service.TokenService;
import com.kmbbj.backend.global.config.jwt.util.JwtTokenizer;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * 현재 사용자의 토큰을 블랙리스트 토큰과 비교를 하고, 유저 사용을 중지 시키는 클래스
 */
@Slf4j
@RequiredArgsConstructor
public class BlacklistFilter extends OncePerRequestFilter {

    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final TokenService tokenService;
    private final BlackListUserService blackListUserService;
    private final JwtTokenizer jwtTokenizer;



    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = getToken(request); // 요청에서 토큰 추출

        if (StringUtils.hasText(token)) {
            Long userId = getUserIdFromToken(token); // 액세스 토큰에서 userId 추출

            if (userId != null) {
                redisToken redisToken = tokenService.getToken(userId); // 서버에 저장된 리프레시 토큰 가져오기

                if (redisToken != null) {
                    String storedRefreshToken = redisToken.getTokenValue(); // 저장된 리프레시 토큰 값을 가져옴

                    if (isTokenBlacklisted(storedRefreshToken)) { // 리프레시 토큰이 블랙리스트에 있는지 확인
                        log.warn("Blacklisted token detected: {}", storedRefreshToken);

                        Optional<User> user = blackListUserService.findById(userId); // 사용자 정보를 조회하여 해당 사용자가 정지되었는지 확인

                        if (user.isPresent()) { // 사용자가 존재하는 경우
                            if (user.get().isSuspended()) { // 사용자가 정지된 상태인지 확인
                                throw new ApiException(ExceptionEnum.USER_SUSPENDED); // 사용자 정지되었을 때
                            } else {
                                throw new ApiException(ExceptionEnum.UNAUTHORIZED); // 유저가 정지되지 않았지만, 블랙리스트에 토큰이 있는 경우
                            }
                        } else {
                            throw new ApiException(ExceptionEnum.USER_NOT_FOUND); // 사용자가 존재하지 않는 경우
                        }
                    }
                } else {
                    log.info("No refresh token found for user ID: {}", userId); // 리프레시 토큰이 없는 경우 로그 기록
                }
            } else {
                log.info("No userId found in token."); // 사용자 ID가 토큰에 포함되어 있지 않은 경우 로그 기록
            }
        }

        filterChain.doFilter(request, response); // 다음 필터로 전달
    }


    /**
     * 요청의 쿠키에서 토큰을 추출
     *
     * @param request 요청 객체
     * @return JWT 토큰
     */
    private String getToken(HttpServletRequest request) {
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
     * 액세스 토큰에서 userId를 추출
     *
     * @param token JWT 액세스 토큰
     * @return userId
     */
    private Long getUserIdFromToken(String token) {
        try {
            Claims claims = jwtTokenizer.parseAccessToken(token);
            return claims.get("userId", Long.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 주어진 토큰이 블랙리스트에 있는지 확인
     *
     * @param token JWT 토큰
     * @return 블랙리스트에 존재하면 true, 그렇지 않으면 false
     */
    private boolean isTokenBlacklisted(String token) {
        return tokenBlacklistRepository.existsByToken(token);
    }
}
