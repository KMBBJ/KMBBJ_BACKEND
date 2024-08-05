package com.kmbbj.backend.global.config.exception;

import com.kmbbj.backend.global.config.reponse.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;

@RestControllerAdvice
public class ApiExceptionAdvice {
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse> exceptionHandler(HttpServletRequest request, ApiException e) {

        ApiExceptionEntity apiExceptionEntity = new ApiExceptionEntity(e.getException().getCode(), e.getException().getMessage());
        ApiResponse apiResponse = new ApiResponse(false, apiExceptionEntity);
        return ResponseEntity.status(e.getException().getStatus())
                .body(apiResponse);
    }


    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse> exceptionHandler(HttpServletRequest request, RuntimeException e) {
        return handleExceptionInternal(ExceptionEnum.ACCESS_DENIED_EXCEPTION);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> exceptionHandler(HttpServletRequest request, Exception e) {
        return handleExceptionInternal(ExceptionEnum.ACCESS_DENIED_EXCEPTION);
    }


    /**
     *
     * 에러 코드 받아서 에러메세지 반환
     */

    private ResponseEntity<ApiResponse> handleExceptionInternal(ExceptionEnum exceptionEnum) {
        return ResponseEntity.status(exceptionEnum.getStatus())
                .body(makeErrorResponse(exceptionEnum));
    }

    /**
     *
     * ApiResponse 를 만드는 함수
     */
    private ApiResponse makeErrorResponse(ExceptionEnum exceptionEnum) {
        ApiExceptionEntity apiExceptionEntity = new ApiExceptionEntity(exceptionEnum.getCode(), exceptionEnum.getMessage());
        return new ApiResponse(false, apiExceptionEntity);
    }
}