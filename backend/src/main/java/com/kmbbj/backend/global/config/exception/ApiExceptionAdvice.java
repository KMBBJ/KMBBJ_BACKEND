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
        ApiResponse apiResponse = new ApiResponse(e.getException().getStatus(), apiExceptionEntity);
        return ResponseEntity.status(e.getException().getStatus())
                .body(apiResponse);
    }
}