package com.kmbbj.backend.global.config.jwt.util;

import com.kmbbj.backend.auth.entity.Authority;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.security.SignatureException;

import java.security.Key;
import java.nio.charset.StandardCharsets;

import java.util.Date;

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

    @PostConstruct
    public void init() {
        accessSecret = accessTokenSecretBase64.getBytes(StandardCharsets.UTF_8);
        refreshSecret = refreshTokenSecretBase64.getBytes(StandardCharsets.UTF_8);
    }


    public String createAccessToken(Long id, String email, String nickname, Authority authority) {
        return createToken(id, email, nickname, authority, accessTokenExpire, accessSecret);
    }

    /**
     * RefreshToken 생성
     *
     * @param id
     * @param email
     * @param nickname
     * @param authority
     * @return RefreshToken
     */
    public String createRefreshToken(Long id, String email, String nickname, Authority authority) {
        return createToken(id, email, nickname, authority, refreshTokenExpire, refreshSecret);
    }

    /**
     * Jwts 빌더를 사용하여 token 생성
     *
     * @param id
     * @param email
     * @param nickname
     * @param authority
     * @param expire
     * @param secretKey
     * @return
     */
    private String createToken(Long id, String email, String nickname, Authority authority, Long expire, byte[] secretKey) {
        // 기본으로 가지고 있는 claim : subject
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

    public Claims parseAccessToken(String accessToken) {
        return parseToken(accessToken, accessSecret);
    }

    public Claims parseRefreshToken(String refreshToken) {
        return parseToken(refreshToken, refreshSecret);
    }

    /**
     * token을 secretkey에 따라서 parse함
     * 클레임을 추출하며 서명 검증, 만료시간을 체크한다.
     *
     * @param token 검사할 jwt토큰
     * @param secretKey 검사할때 사용되는 secretkey
     * @return
     */
    public Claims parseToken(String token, byte[] secretKey) {
        Claims claims = null;
        try {
            claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey(secretKey))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (SignatureException e) { // 토큰 유효성 체크 실패 시
            throw new RuntimeException(String.valueOf(HttpStatus.UNAUTHORIZED));
        }

        return claims;
    }

    /**
     * @param secretKey - byte형식
     * @return Key 형식 시크릿 키
     */
    public static Key getSigningKey(byte[] secretKey) {
        return Keys.hmacShaKeyFor(secretKey);
    }

    public long getAccessTokenExpire() {
        return accessTokenExpire;
    }
}
