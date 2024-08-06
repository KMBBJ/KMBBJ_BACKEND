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

    // 헤더 포함해서 성공메시지만 전달할 경우
    public ApiResponse(String message) {
        this.message = message;
    }

    // 헤더 포함해서 성공메시지 및 필요한 Data 같이 전달할 경우
    public ApiResponse(String message, T data) {
        this.message = message;
        this.data = data;
    }

    // 헤더 미포함 ApiResponse 만 응답할때
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
