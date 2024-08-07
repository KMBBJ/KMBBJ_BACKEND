package com.kmbbj.backend.global.config.reponse;

import com.kmbbj.backend.global.config.exception.ApiExceptionEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "공통 응답")
public class CustomResponse<T> {
    private HttpStatus status;
    private String message;
    private T data;

    // 헤더 포함해서 성공메시지만 전달할 경우
    public CustomResponse(String message) {
        this.message = message;
    }

    // 헤더 포함해서 성공메시지 및 필요한 Data 같이 전달할 경우
    public CustomResponse(String message, T data) {
        this.message = message;
        this.data = data;
    }

    // 헤더 미포함 ApiResponse 만 응답할때
    public CustomResponse(HttpStatus status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
}