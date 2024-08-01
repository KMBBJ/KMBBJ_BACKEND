package com.kmbbj.backend.global.config.jwt.infrastructure;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Objects;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {
    private final transient Object principal;
    private final transient Object credentials;

    /**
     * 인증된 토큰을 생성하는 생성자
     *
     * @param authorities 권한
     * @param principal   주체 (사용자)
     * @param credentials 인증 정보 (자격 증명)
     */
    public JwtAuthenticationToken(Collection<? extends GrantedAuthority> authorities, Object principal, Object credentials) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        this.setAuthenticated(true); // 토큰을 인증된 상태로 설정
    }

    /**
     * 자격 증명을 반환
     *
     * @return 자격 증명
     */
    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    /**
     * 주체 (사용자)를 반환
     *
     * @return 주체 (사용자)
     */
    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        JwtAuthenticationToken that = (JwtAuthenticationToken) o;
        return Objects.equals(principal, that.principal) &&
                Objects.equals(credentials, that.credentials);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), principal, credentials);
    }
}
