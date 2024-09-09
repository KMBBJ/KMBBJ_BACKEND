package com.kmbbj.backend.feature.auth.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "사용자 별명 변경에 사용되는 Dto")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNickNameChangeRequest {
    @Schema(description = "사용자 변경할 별명", example = "김창성2")
    @NotEmpty(message = "변경할 이름은 필수 입니다.")
    private String nickName;
}
