package com.kmbbj.backend.auth.controller;

import com.kmbbj.backend.auth.controller.request.UserJoinRequest;
import com.kmbbj.backend.auth.controller.request.UserLoginRequest;
import com.kmbbj.backend.auth.service.UserService;
import com.kmbbj.backend.auth.entity.User;
import com.kmbbj.backend.global.config.jwt.entity.redisToken;
import com.kmbbj.backend.global.config.jwt.service.TokenService;
import com.kmbbj.backend.global.config.jwt.util.JwtTokenizer;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenizer jwtTokenizer;
    private final TokenService tokenService;

    /**
     * 사용자를 로그인하고 JWT 토큰을 생성하여 반환합니다.
     *
     * @param userLoginRequest 로그인 요청 데이터
     * @param bindingResult    요청 데이터 검증 결과
     * @param response         HTTP 응답 객체
     * @return 로그인 결과를 나타내는 ResponseEntity
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody @Valid UserLoginRequest userLoginRequest, BindingResult bindingResult, HttpServletResponse response) {
        // 필드 에러 확인
        if (bindingResult.hasErrors()) {
            throw new RuntimeException(HttpStatus.BAD_REQUEST.getReasonPhrase());
        }
        User user = userService.UserfindByEmail(userLoginRequest.getEmail()).orElseThrow(() -> new RuntimeException("사용자 이메일이 없습니다.: " + userLoginRequest.getEmail()));

        // 비밀번호 일치여부 체크
        if (!passwordEncoder.matches(userLoginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException(HttpStatus.BAD_REQUEST.getReasonPhrase());
        }

        // 액세스 토큰 및 리프레시 토큰 생성
        String accessToken = jwtTokenizer.createAccessToken(user.getId(), user.getEmail(), user.getNickname(), user.getAuthority());
        String refreshToken = jwtTokenizer.createRefreshToken(user.getId(), user.getEmail(), user.getNickname(), user.getAuthority());

        // 리프레시 토큰 저장
        tokenService.saveOrRefresh(new redisToken(user.getId(), refreshToken, tokenService.calculateTimeout()));

        // 액세스 토큰을 쿠키에 추가
        Cookie accessTokenCookie = new Cookie("Access-Token", accessToken);
        accessTokenCookie.setPath("/");      // 모든 경로에서 유효
        accessTokenCookie.setMaxAge((int) jwtTokenizer.getAccessTokenExpire()); // 액세스 토큰 만료 시간 설정
        response.addCookie(accessTokenCookie);

        // 리프레시 토큰을 응답 헤더에 추가
        response.setHeader("Refresh-Token", refreshToken);

        return ResponseEntity.ok("로그인 되었습니다.");
    }

    /**
     * 로그아웃 처리
     *
     * @param request  HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @return 로그아웃 결과 메시지를 포함한 ResponseEntity
     */
    @DeleteMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        // 클라이언트 쿠키에서 "Access-Token" 쿠키 제거
        Cookie accessTokenCookie = new Cookie("Access-Token", null);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0); // 쿠키 만료
        response.addCookie(accessTokenCookie);

        // 요청 헤더에서 "Refresh-Token" 제거
        String refreshToken = request.getHeader("Refresh-Token");
        if (refreshToken != null) {
            // 리프레시 토큰 무효화 로직을 추가합니다.
            tokenService.invalidateRefreshToken(refreshToken);
        }

        return ResponseEntity.ok("로그아웃 되었습니다.");
    }

    /**
     * 회원가입 처리
     *
     * @param userJoinRequest 회원가입 요청 데이터
     * @param bindingResult   요청 데이터 검증 결과
     * @return 회원가입 결과를 나타내는 ResponseEntity
     */
    @PostMapping("/join")
    public ResponseEntity<?> join(@RequestBody @Valid UserJoinRequest userJoinRequest, BindingResult bindingResult) {
        // 필드 에러 확인
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }

        // 회원가입 서비스 호출
        try {
            userService.registerUser(userJoinRequest);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입이 완료되었습니다.");
    }
}
