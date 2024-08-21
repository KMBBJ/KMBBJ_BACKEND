package com.kmbbj.backend.admin.controller;


import com.kmbbj.backend.admin.entity.AdminAlarm;
import com.kmbbj.backend.admin.service.AdminService;
//import com.kmbbj.backend.admin.service.NotificationService;
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
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
//    private final NotificationService notificationService;

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
                .body(response); // 바디에 결과 추가
    }




    /**
     * 알람 및 로그인된 사용자 정보 조회
     *
     * @param page 첫 페이지 0 기본값
     * @param size 페이지당 보여지는 공지 수
     * @return 유저 정보 및 공지사항 정보 리스트 반환 ResponseEntity
     */
    @GetMapping
    @Operation(summary = "알림 및 로그인된 사용자 정보 조회", description = "알림 리스트와 로그인된 사용자 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "알림 및 사용자 정보 조회 성공"),
            @ApiResponse(responseCode = "404", description = "알림 또는 사용자 정보를 찾지 못했습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    public ResponseEntity<CustomResponse<Map<String, Object>>> getAllAlarmsAndAuthenticatedUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageRequest pageRequest = PageRequest.of(page, size);

        // 공지사항 조회
        Page<AdminAlarm> adminAlarmsPage = adminService.findAllAdminAlarm(pageRequest);

        // 공지사항의 제목, 내용 추출
        List<Map<String, String>> alarms = adminAlarmsPage.getContent().stream()
                .map(alarm -> {
                    Map<String, String> alarmData = new HashMap<>();
                    alarmData.put("title", alarm.getTitle()); //제목 추출
                    alarmData.put("content", alarm.getContent()); // 내용 추출
                    return alarmData; // 각 알람 객체 변환 후 반환
                })
                .collect(Collectors.toList()); // 리턴 값을 리스트 형태로 변환

        // 로그인된 사용자 정보 조회
        User authenticatedUser = adminService.getAuthenticatedUser();
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("name", authenticatedUser.getNickname()); // 해당 유저 이름 추출
        userInfo.put("email", authenticatedUser.getEmail()); // 해당 유저 이메일 추출
        userInfo.put("type", String.valueOf(authenticatedUser.getAuthority())); // 해당 유저의 타입 admin/user 값 추출

        // 알람과 회원 정보를 하나의 Map으로 통합
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("alarms", alarms);
        responseData.put("userInfo", userInfo);

        // 성공적인 응답 생성
        CustomResponse<Map<String, Object>> response = new CustomResponse<>(HttpStatus.OK, "알림 및 로그인된 사용자 정보 조회 성공", responseData);
        return ResponseEntity.ok()
                .body(response);
    }

    /**
     * 알림 추가 및 전송 메서드
     *
     * @param adminAlarm 알람 관련
     * @return 유저 리스트 정보 담긴 ResponseEntity
     */
    @PostMapping("/add")
    @Operation(summary = "알림 추가", description = "새로운 알림을 추가합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "알림 추가 성공"),
            @ApiResponse(responseCode = "404", description = "알림 또는 사용자 정보를 찾지 못했습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    public ResponseEntity<CustomResponse<AdminAlarm>> addAlarm(
            @RequestBody AdminAlarm adminAlarm) {

        // 로그인된 사용자 정보 가져오기
        Long id = adminService.getAuthenticatedUser().getId();

        // 알람 저장
        AdminAlarm savedAlarm = adminService.saveAlarm(id, adminAlarm);

        // 실시간 알림 전송 (오류로 인한 임시 주석 처리)
//        notificationService.sendNotification("/topic", savedAlarm);

        // 응답 데이터 생성
        CustomResponse<AdminAlarm> response = new CustomResponse<>(HttpStatus.OK, "알림 추가 성공", savedAlarm);

        return ResponseEntity.ok()
                .body(response); // 바디에 결과 추가
    }

}
