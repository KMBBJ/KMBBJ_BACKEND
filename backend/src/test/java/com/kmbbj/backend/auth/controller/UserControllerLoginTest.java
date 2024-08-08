package com.kmbbj.backend.auth.controller;

import com.kmbbj.backend.auth.controller.request.UserLoginRequest;
import com.kmbbj.backend.auth.service.UserService;
import com.kmbbj.backend.auth.entity.User;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import com.kmbbj.backend.global.config.jwt.entity.redisToken;
import com.kmbbj.backend.global.config.jwt.service.TokenService;
import com.kmbbj.backend.global.config.jwt.util.JwtTokenizer;
import com.kmbbj.backend.global.config.reponse.CustomResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserControllerLoginTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenizer jwtTokenizer;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private UserController userController;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void loginSuccessTest() {
        UserLoginRequest userLoginRequest = UserLoginRequest.builder()
                .email("test@example.com")
                .password("Password123!")
                .build();

        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("Password123!")
                .build();

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.UserfindByEmail(userLoginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(userLoginRequest.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtTokenizer.createAccessToken(user.getId(), user.getEmail(), user.getNickname(), user.getAuthority())).thenReturn("accessToken");
        when(jwtTokenizer.createRefreshToken(user.getId(), user.getEmail(), user.getNickname(), user.getAuthority())).thenReturn("refreshToken");
        when(tokenService.calculateTimeout()).thenReturn(LocalDateTime.now().plusHours(1));

        CustomResponse<String> customResponse = userController.login(userLoginRequest, bindingResult, response);

        assertEquals(HttpStatus.OK, customResponse.getStatus());
        assertEquals("로그인 성공", customResponse.getMessage());
        verify(response).addCookie(any(Cookie.class));
        verify(response).setHeader("Refresh-Token", "refreshToken");
        verify(tokenService).saveOrRefresh(any(redisToken.class));
    }

    @Test
    void loginFieldErrorsTest() {
        UserLoginRequest userLoginRequest = UserLoginRequest.builder()
                .email("test@example.com")
                .password("Password123!")
                .build();

        when(bindingResult.hasErrors()).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> userController.login(userLoginRequest, bindingResult, response));
        assertEquals(ExceptionEnum.NOT_ALLOW_FILED, exception.getException());
    }

    @Test
    void loginUserNotFoundTest() {
        UserLoginRequest userLoginRequest = UserLoginRequest.builder()
                .email("test@example.com")
                .password("Password123!")
                .build();

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.UserfindByEmail(userLoginRequest.getEmail())).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> userController.login(userLoginRequest, bindingResult, response));
        assertEquals(ExceptionEnum.USER_NOT_FOUND, exception.getException());
    }

    @Test
    void loginPasswordMismatchTest() {
        UserLoginRequest userLoginRequest = UserLoginRequest.builder()
                .email("test@example.com")
                .password("Password123!")
                .build();

        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("Password123!")
                .build();

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.UserfindByEmail(userLoginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(userLoginRequest.getPassword(), user.getPassword())).thenReturn(false);

        ApiException exception = assertThrows(ApiException.class, () -> userController.login(userLoginRequest, bindingResult, response));
        assertEquals(ExceptionEnum.DIFFERENT_PASSWORD, exception.getException());
    }
}