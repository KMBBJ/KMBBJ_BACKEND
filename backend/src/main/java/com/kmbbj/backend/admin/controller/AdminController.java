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

    /**
     * 유저 검색 / 리스트 조회
     *
     * @param page 첫 페이지 0 기본값
     * @param size 페이지당 유저 수
     * @param email 검색할 이메일 글자
     * @return 유저 리스트 정보 담긴 ResponseEntity
     */
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

        Map<String, Object> result = new HashMap<>(); // 결과를 저장할 맵 초기화

        // 이메일이 제공된 경우 유저 검색
        if (email != null && !email.isEmpty()) {
            List<User> searchedUsers = adminService.searchUserByEmail(email);
            if (searchedUsers.isEmpty()) {
                throw new ApiException(ExceptionEnum.EMAIL_NOT_FOUND); // 유저를 찾지 못한 경우 예외 발생
            }
            result.put("userList", searchedUsers); // 검색된 유저 리스트 추가
        } else {
            // 이메일이 제공되지 않은 경우 모든 유저 리스트 조회
            Pageable pageable = PageRequest.of(page, size); // 페이지 요청 정보 생성
            Page<User> users = adminService.findAllUser(pageable); // 전체 유저 조회
            result.put("userList", users.getContent()); // 전체 유저 리스트 추가
        }

        // 성공적인 응답 생성
        CustomResponse<Map<String, Object>> response = new CustomResponse<>(HttpStatus.OK, "유저 리스트 조회 성공", result);
        return ResponseEntity.ok()
                .header("Custom-Header", "value") // 커스텀 헤더 추가
                .body(response); //바디에 결과 추가
    }
}
