package com.kmbbj.backend.global.config.reponse;

import com.kmbbj.backend.global.config.exception.ApiExceptionEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "공통 예외")
public class ErrorResponse {
    private HttpStatus status;
    private ApiExceptionEntity exception;

    // 실패
    public ErrorResponse(HttpStatus status, ApiExceptionEntity exception) {
        this.status = status;
        this.exception = exception;
    }
}
