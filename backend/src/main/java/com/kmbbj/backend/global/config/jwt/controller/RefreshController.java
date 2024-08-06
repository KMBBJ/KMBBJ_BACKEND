package com.kmbbj.backend.global.config.jwt.controller;

import com.kmbbj.backend.auth.entity.Authority;
import com.kmbbj.backend.global.config.jwt.entity.redisToken;
import com.kmbbj.backend.global.config.jwt.service.TokenService;
import com.kmbbj.backend.global.config.jwt.util.JwtTokenizer;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/token")
@RequiredArgsConstructor
public class RefreshController {

    private final JwtTokenizer jwtTokenizer;
    private final TokenService tokenService;

    /**
     * 리프레시 토큰을 사용하여 새로운 액세스 토큰과 리프레시 토큰을 발급합니다.
     *
     * @param request  리프레시 토큰이 포함된 HTTP 요청 객체
     * @param response 새로운 토큰을 추가할 HTTP 응답 객체
     * @return 토큰 갱신 결과를 나타내는 ResponseEntity
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshTokens(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = getRefreshTokenFromRequest(request);
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("리프레시 토큰이 없습니다.");
        }

        try {
            Claims claims = jwtTokenizer.parseRefreshToken(refreshToken);
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

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 리프레시 토큰입니다.");
        }
    }

    /**
     * 요청 헤더에서 리프레시 토큰을 추출합니다.
     *
     * @param request HTTP 요청 객체
     * @return 추출된 리프레시 토큰, 없을 경우 null
     */
    private String getRefreshTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}