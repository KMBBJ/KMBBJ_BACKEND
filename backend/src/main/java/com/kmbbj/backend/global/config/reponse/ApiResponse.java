package com.kmbbj.backend.global.config.reponse;

import com.kmbbj.backend.global.config.exception.ApiExceptionEntity;
import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@NoArgsConstructor
public class ApiResponse<T> {
    private HttpStatus status;
    private String message;
    private ApiExceptionEntity exception;
    private T data;

    // 성공메시지만 전달할 경우
    public ApiResponse(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    // 성공메시지 및 필요한 Data 같이 전달할 경우
    public ApiResponse(HttpStatus status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    // 실패
    public ApiResponse(HttpStatus status, ApiExceptionEntity exception) {
        this.status = status;
        this.exception = exception;
    }
}
