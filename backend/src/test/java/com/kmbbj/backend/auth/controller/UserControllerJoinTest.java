package com.kmbbj.backend.auth.controller;

import com.kmbbj.backend.auth.controller.request.UserJoinRequest;
import com.kmbbj.backend.auth.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

        ResponseEntity<?> responseEntity = userController.join(userJoinRequest, bindingResult);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals("회원가입이 완료되었습니다.", responseEntity.getBody());
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
        when(bindingResult.getAllErrors()).thenReturn(List.of(new ObjectError("error", "error")));

        ResponseEntity<?> responseEntity = userController.join(userJoinRequest, bindingResult);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }
}
