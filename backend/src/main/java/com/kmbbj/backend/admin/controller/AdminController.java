package com.kmbbj.backend.admin.controller;


import com.kmbbj.backend.admin.service.AdminService;
import com.kmbbj.backend.auth.entity.User;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import com.kmbbj.backend.global.config.reponse.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;


    // 유저 검색 / 리스트 조회
    @GetMapping("/user_search")
    @Operation(summary = "유저 검색 및 리스트 조회", description = "이메일을 통한 유저 검색 및 유저 리스트 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "유저 리스트 조회 성공"),
            @ApiResponse(responseCode = "404", description = "유저를 찾지 못했습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    public ResponseEntity<CustomResponse<Map<String, Object>>> userListScreen(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String email) {

        Map<String, Object> result = new HashMap<>();

        if (email != null && !email.isEmpty()) {
            // 이메일 검색 결과
            List<User> searchedUsers = adminService.searchUserByEmail(email);
            if (searchedUsers.isEmpty()) {
                throw new ApiException(ExceptionEnum.EMAIL_NOT_FOUND);
            }
            result.put("userList", searchedUsers);
        } else {
            // 유저 리스트
            Pageable pageable = PageRequest.of(page, size);
            Page<User> users = adminService.findAllUser(pageable);
            result.put("userList", users.getContent());
        }

        CustomResponse<Map<String, Object>> response = new CustomResponse<>(HttpStatus.OK, "유저 리스트 조회 성공", result);
        return ResponseEntity.ok()
                .header("Custom-Header", "value")
                .body(response);
    }

}
