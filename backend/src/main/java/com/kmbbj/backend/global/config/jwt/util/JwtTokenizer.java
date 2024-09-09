package com.kmbbj.backend.global.config.jwt.util;

import com.kmbbj.backend.feature.auth.entity.Authority;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.security.SignatureException;

import java.security.Key;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 토큰을 생성하고 검증하는 유틸리티 클래스
 */
@Component
public class JwtTokenizer {

    @Value("${JWT_ACCESSSECRET}")
    private String accessTokenSecretBase64;

    @Value("${JWT_REFESHSECRET}")
    private String refreshTokenSecretBase64;

    @Value("${JWT_ACCESSTOKENEXPIRE}")
    private long accessTokenExpire;

    @Value("${JWT_REFRESHTOKENEXPIRE}")
    private long refreshTokenExpire;

    private byte[] accessSecret;
    private byte[] refreshSecret;

    /**
     * 초기화 메서드. Base64로 인코딩된 비밀키를 바이트 배열로 변환
     */
    @PostConstruct
    public void init() {
        accessSecret = accessTokenSecretBase64.getBytes(StandardCharsets.UTF_8);
        refreshSecret = refreshTokenSecretBase64.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 액세스 토큰을 생성
     *
     * @param id 사용자 ID
     * @param email 사용자 이메일
     * @param nickname 사용자 닉네임
     * @param authority 사용자 권한
     * @return 생성된 액세스 토큰
     */
    public String createAccessToken(Long id, String email, String nickname, Authority authority) {
        return createToken(id, email, nickname, authority, accessTokenExpire, accessSecret);
    }

    /**
     * 리프레시 토큰을 생성
     *
     * @param id 사용자 ID
     * @param email 사용자 이메일
     * @param nickname 사용자 닉네임
     * @param authority 사용자 권한
     * @return 생성된 리프레시 토큰
     */
    public String createRefreshToken(Long id, String email, String nickname, Authority authority) {
        return createToken(id, email, nickname, authority, refreshTokenExpire, refreshSecret);
    }

    /**
     * JWT 토큰을 생성
     *
     * @param id 사용자 ID
     * @param email 사용자 이메일
     * @param nickname 사용자 닉네임
     * @param authority 사용자 권한
     * @param expire 토큰 만료 시간
     * @param secretKey 비밀키
     * @return 생성된 JWT 토큰
     */
    private String createToken(Long id, String email, String nickname, Authority authority, Long expire, byte[] secretKey) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("authority", authority);
        claims.put("userId", id);
        claims.put("nickname", nickname);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + expire))
                .signWith(getSigningKey(secretKey))
                .compact();
    }

    /**
     * 액세스 토큰을 파싱하여 클레임을 반환
     *
     * @param accessToken 액세스 토큰
     * @return 토큰에서 추출된 클레임
     */
    public Claims parseAccessToken(String accessToken) {
        return parseToken(accessToken, accessSecret);
    }

    /**
     * 리프레시 토큰을 파싱하여 클레임을 반환
     *
     * @param refreshToken 리프레시 토큰
     * @return 토큰에서 추출된 클레임
     */
    public Claims parseRefreshToken(String refreshToken) {
        return parseToken(refreshToken, refreshSecret);
    }

    /**
     * JWT 토큰을 파싱하여 클레임을 반환
     *
     * @param token JWT 토큰
     * @param secretKey 비밀키
     * @return 토큰에서 추출된 클레임
     */
    public Claims parseToken(String token, byte[] secretKey) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey(secretKey))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new ApiException(ExceptionEnum.EXPIRED_TOKEN);
        } catch (UnsupportedJwtException e) {
            throw new ApiException(ExceptionEnum.UNSUPPORTED_TOKEN);
        } catch (MalformedJwtException e) {
            throw new ApiException(ExceptionEnum.INVALID_TOKEN);
        } catch (SignatureException e) {
            throw new ApiException(ExceptionEnum.INVALID_SIGNATURE);
        } catch (IllegalArgumentException e) {
            throw new ApiException(ExceptionEnum.ILLEGAL_ARGUMENT);
        }
    }

    /**
     * 비밀키를 HMAC SHA로 변환하여 반환
     *
     * @param secretKey 비밀키
     * @return 변환된 시크릿 키
     */
    public static Key getSigningKey(byte[] secretKey) {
        return Keys.hmacShaKeyFor(secretKey);
    }

    /**
     * 액세스 토큰 만료 시간을 반환
     *
     * @return 액세스 토큰 만료 시간
     */
    public long getAccessTokenExpire() {
        return accessTokenExpire;
    }
}