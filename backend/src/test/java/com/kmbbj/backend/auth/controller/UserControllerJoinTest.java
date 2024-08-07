package com.kmbbj.backend.auth.controller;

import com.kmbbj.backend.auth.controller.request.UserJoinRequest;
import com.kmbbj.backend.auth.service.UserService;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import com.kmbbj.backend.global.config.reponse.CustomResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserControllerJoinTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Mock
    private BindingResult bindingResult;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void joinSuccessTest() {
        UserJoinRequest userJoinRequest = UserJoinRequest.builder()
                .email("test@example.com")
                .password("Password123!")
                .passwordCheck("Password123!")
                .build();

        when(bindingResult.hasErrors()).thenReturn(false);

        CustomResponse<String> customResponse = userController.join(userJoinRequest, bindingResult);

        assertEquals(HttpStatus.CREATED, customResponse.getStatus());
        assertEquals("회원가입 성공", customResponse.getMessage());
        assertEquals("회원가입이 완료되었습니다.", customResponse.getData());
        verify(userService).registerUser(userJoinRequest);
    }

    @Test
    void joinFailureTest() {
        UserJoinRequest userJoinRequest = UserJoinRequest.builder()
                .email("invalid-email")
                .password("password")
                .passwordCheck("password")
                .build();

        when(bindingResult.hasErrors()).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> userController.join(userJoinRequest, bindingResult));
        assertEquals(ExceptionEnum.NOT_ALLOW_FILED, exception.getException());
    }
}