package com.kmbbj.backend.global.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 설정 클래스
 * 애플리케이션의 보안 구성을 정의
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // 모든 유저 허용 페이지
    String[] allAllowPage = new String[]{
            "/", // 메인페이지
            "/error", // 에러페이지
            "/test/**", // 테스트 페이지
            "/auth/refreshToken" // 토큰 재발급 페이지
    };

    // 관리자 유저 허용 페이지
    String[] adminAllowPage = new String[]{
            "/", // 메인페이지
            "/error", // 에러페이지
            "/test/**", // 테스트 페이지
            "/auth/refreshToken" // 토큰 재발급 페이지
    };

    // 비로그인 유저 허용 페이지
    String[] notLoggedAllowPage = new String[]{
            "/auth/login", // 로그인 페이지
            "/auth/join" // 회원가입 페이지
    };


    /**
     * 보안 필터 체인
     *
     * @param http 수정할 HttpSecurity 객체
     * @return 구성된 SecurityFilterChain
     * @throws Exception HttpSecurity 구성 시 발생한 예외
     */
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 유저별 페이지 접근 허용
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(allAllowPage).permitAll() // 모든 유저
                .requestMatchers(adminAllowPage).hasRole("ADMIN") //관리자
                .requestMatchers(notLoggedAllowPage).not().authenticated() // 비로그인 유저
                .anyRequest().authenticated()
        );

        // 세션 관리 Stateless 설정(서버가 클라이언트 상태 저장x)
        http.sessionManagement(auth -> auth.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // cors 허용
        http.csrf(csrf -> csrf.disable());

        // 로그인 폼 비활성화
        http.formLogin(auth -> auth.disable());

        // http 기본 인증(헤더) 비활성화
        http.httpBasic(auth -> auth.disable());

        // SecurityFilterChain을 빌드 후 반환
        return http.build();

    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
