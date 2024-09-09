package com.kmbbj.backend.feature.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

@Getter
@AllArgsConstructor
public enum Authority implements GrantedAuthority {
    ROLE_ADMIN,
    ROLE_USER;

    /**
     * 유저의 권한을 String으로 반환하는 메소드
     * @return authority name
     */
    @Override
    public String getAuthority() {
        return name();
    }
}