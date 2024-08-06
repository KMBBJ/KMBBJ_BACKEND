package com.kmbbj.backend.auth.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Schema(description = "회원가입할때 사용하는 폼")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class UserJoinRequest {
    @Schema(description = "사용자의 이메일 주소", example = "user@example.com")
    @NotBlank(message = "이메일을 입력해주세요.")
    @Pattern(regexp = "^[0-9a-zA-Z]([-_\\.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_\\.]?[0-9a-zA-Z])*\\.[a-zA-Z]{2,3}$",
            message = "이메일 형식으로 입력해주세요.")
    private String email;

    @Schema(description = "사용자의 비밀번호", example = "Password123!")
    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*?_]).{8,16}$",
            message = "영어, 숫자, 특수문자를 포함한 8~16자 비밀번호를 입력해주세요.")
    private String password;

    @Schema(description = "비밀번호 확인", example = "Password123!")
    @JsonProperty("password_check")
    @NotEmpty(message = "비밀번호 확인을 입력해주세요.")
    private String passwordCheck;
}