package com.kmbbj.backend.auth.controller;

import com.kmbbj.backend.auth.controller.request.UserIdRequest;
import com.kmbbj.backend.auth.controller.response.UserProfileReponse;
import com.kmbbj.backend.auth.service.UserService;
import com.kmbbj.backend.global.config.reponse.CustomResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
@Tag(name = "profile", description = "사용자 프로파일")
public class ProfileController {
    private final UserService userService;

    @PostMapping("/")
    public CustomResponse<UserProfileReponse> profile(@RequestBody UserIdRequest userIdRequest) {
        return new CustomResponse<>(HttpStatus.OK,"사용자 프로필", userService.UserProfilefindByUserId(userIdRequest.getUserId()));
    }
}