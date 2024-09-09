package com.kmbbj.backend.feature.auth.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "사용자 비밀번호 변경에 사용되는 요청 DTO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPasswordChangeRequest {
    @Schema(description = "사용자의 비밀번호", example = "Password123!")
    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*?_]).{8,16}$",
            message = "영어, 숫자, 특수문자를 포함한 8~16자 비밀번호를 입력해주세요.")
    private String password;
}
