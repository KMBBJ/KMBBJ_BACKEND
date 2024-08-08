package com.kmbbj.backend.auth.controller;

import com.kmbbj.backend.auth.controller.request.UserJoinRequest;
import com.kmbbj.backend.auth.controller.request.UserLoginRequest;
import com.kmbbj.backend.auth.service.UserService;
import com.kmbbj.backend.auth.entity.User;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import com.kmbbj.backend.global.config.jwt.entity.redisToken;
import com.kmbbj.backend.global.config.jwt.service.TokenService;
import com.kmbbj.backend.global.config.jwt.util.JwtTokenizer;
import com.kmbbj.backend.global.config.reponse.CustomResponse;
import com.kmbbj.backend.global.config.security.FindUserBySecurity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "사용자 인증 API")
public class UserController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenizer jwtTokenizer;
    private final TokenService tokenService;
    private final FindUserBySecurity findUserBySecurity;

    /**
     * 사용자를 로그인하고 JWT 토큰을 생성하여 반환
     *
     * @param userLoginRequest 로그인 요청 데이터
     * @param bindingResult    요청 데이터 검증 결과
     * @param response         HTTP 응답 객체
     * @return 로그인 결과
     */
    @PostMapping("/login")
    @Operation(summary = "사용자 로그인", description = "사용자를 인증하고 JWT 토큰을 반환")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "사용자 찾을 수 없음"),
            @ApiResponse(responseCode = "401", description = "비밀번호 불일치")
    })
    public CustomResponse<String> login(@RequestBody @Valid UserLoginRequest userLoginRequest, BindingResult bindingResult, HttpServletResponse response) {
        // 필드 에러 확인
        if (bindingResult.hasErrors()) {
            throw new ApiException(ExceptionEnum.NOT_ALLOW_FILED);
        }
        User user = userService.UserfindByEmail(userLoginRequest.getEmail()).orElseThrow(() -> new ApiException(ExceptionEnum.USER_NOT_FOUND));

        // 비밀번호 일치여부 체크
        if (!passwordEncoder.matches(userLoginRequest.getPassword(), user.getPassword())) {
            throw new ApiException(ExceptionEnum.DIFFERENT_PASSWORD);
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

        return new CustomResponse<>(HttpStatus.OK, "로그인 성공", "로그인 되었습니다.");
    }

    /**
     * 로그아웃 처리
     *
     * @param request  HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @return 로그아웃 결과
     */
    @DeleteMapping("/logout")
    @Operation(summary = "사용자 로그아웃", description = "사용자를 로그아웃하고 토큰을 무효화")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public CustomResponse<String> logout(HttpServletRequest request, HttpServletResponse response) {
        Long userId = findUserBySecurity.getCurrentUser().getId();

        // 클라이언트 쿠키에서 "Access-Token" 쿠키 제거
        Cookie accessTokenCookie = new Cookie("Access-Token", null);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0); // 쿠키 만료
        response.addCookie(accessTokenCookie);

        tokenService.invalidateRefreshToken(userId);

        return new CustomResponse<>(HttpStatus.OK, "로그아웃 성공", "로그아웃 되었습니다.");
    }

    /**
     * 회원가입 처리
     *
     * @param userJoinRequest 회원가입 요청 데이터
     * @param bindingResult   요청 데이터 검증 결과
     * @return 회원가입 결과
     */
    @PostMapping("/join")
    @Operation(summary = "사용자 회원가입", description = "새로운 사용자를 등록")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 사용자")
    })
    public CustomResponse<String> join(@RequestBody @Valid UserJoinRequest userJoinRequest, BindingResult bindingResult) {
        // 필드 에러 확인
        if (bindingResult.hasErrors()) {
            throw new ApiException(ExceptionEnum.NOT_ALLOW_FILED);
        }

        // 회원가입 서비스 호출
        userService.registerUser(userJoinRequest);

        return new CustomResponse<>(HttpStatus.CREATED, "회원가입 성공", "회원가입이 완료되었습니다.");
    }
}