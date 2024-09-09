package com.kmbbj.backend.global.config.jwt.controller;

import com.kmbbj.backend.feature.auth.entity.Authority;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import com.kmbbj.backend.global.config.jwt.entity.redisToken;
import com.kmbbj.backend.global.config.jwt.service.TokenService;
import com.kmbbj.backend.global.config.jwt.util.JwtTokenizer;
import com.kmbbj.backend.global.config.reponse.CustomResponse;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/token")
@RequiredArgsConstructor
@Tag(name = "Token", description = "JWT 토큰 갱신 API")
public class RefreshController {

    private final JwtTokenizer jwtTokenizer;
    private final TokenService tokenService;

    /**
     * 리프레시 토큰을 사용하여 새로운 액세스 토큰과 리프레시 토큰을 발급
     *
     * @param request  리프레시 토큰이 포함된 HTTP 요청 객체
     * @param response 새로운 토큰을 추가할 HTTP 응답 객체
     * @return 토큰 갱신 결과
     */
    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "리프레시 토큰을 사용하여 새로운 액세스 토큰과 리프레시 토큰을 발급")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "새로운 토큰 발급 완료", content = @Content(schema = @Schema(implementation = CustomResponse.class))),
            @ApiResponse(responseCode = "401", description = "리프레시 토큰이 없음", content = @Content(schema = @Schema(implementation = CustomResponse.class))),
            @ApiResponse(responseCode = "403", description = "유효하지 않은 리프레시 토큰", content = @Content(schema = @Schema(implementation = CustomResponse.class)))
    })
    public CustomResponse<Long> refreshTokens(
            HttpServletRequest request,
            HttpServletResponse response) {
        String refreshToken = getRefreshTokenFromRequest(request);
        if (refreshToken == null) {
            throw new ApiException(ExceptionEnum.TOKEN_NOT_FOUND);
        }

        Claims claims = jwtTokenizer.parseRefreshToken(refreshToken);
        Long userId = claims.get("userId", Long.class);
        String email = claims.get("email", String.class);
        String nickname = claims.get("nickname", String.class);
        Authority authority = Authority.valueOf(claims.get("authority", String.class));

        String newAccessToken = jwtTokenizer.createAccessToken(userId, email, nickname, authority);
        String newRefreshToken = jwtTokenizer.createRefreshToken(userId, email, nickname, authority);

        // 새로운 리프레시 토큰을 데이터베이스에 저장
        tokenService.saveOrRefresh(new redisToken(userId, newRefreshToken, tokenService.calculateTimeout()));

        // 새로운 리프레시 토큰을 응답 헤더에 추가
        response.setHeader("Refresh-Token", newRefreshToken);
        response.setHeader("Access-Token", newAccessToken);

        return new CustomResponse<>(HttpStatus.OK, "새로운 토큰 발급 완료", userId);
    }

    /**
     * 요청 헤더에서 리프레시 토큰을 추출
     *
     * @param request HTTP 요청 객체
     * @return 추출된 리프레시 토큰, 없을 경우 null
     */
    private String getRefreshTokenFromRequest(HttpServletRequest request) {
        // 헤더에서 "Refresh-Token" 값을 가져옴
        String refreshToken = request.getHeader("Refresh-Token");
        if (refreshToken != null && !refreshToken.isEmpty()) {
            return refreshToken;
        }
        return null;
    }
}