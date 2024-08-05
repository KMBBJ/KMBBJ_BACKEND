package com.kmbbj.backend.global.config.reponse;

import com.kmbbj.backend.global.config.exception.ApiExceptionEntity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private ApiExceptionEntity exception;
    private T data;

    // 성공메시지만 전달할 경우
    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // 성공메시지 및 필요한 Data 같이 전달할 경우
    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // 실패
    public ApiResponse(boolean success, ApiExceptionEntity exception) {
        this.success = success;
        this.exception = exception;
    }
}
