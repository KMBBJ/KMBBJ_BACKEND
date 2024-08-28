package com.kmbbj.backend.admin.controller;


import com.kmbbj.backend.admin.dto.ExampleRequestDTO;
import com.kmbbj.backend.admin.entity.AdminAlarm;
import com.kmbbj.backend.admin.service.AdminService;
//import com.kmbbj.backend.admin.service.NotificationService;
import com.kmbbj.backend.admin.service.BlackListUserService;
import com.kmbbj.backend.auth.entity.User;
import com.kmbbj.backend.auth.repository.UserRepository;
import com.kmbbj.backend.auth.service.UserService;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import com.kmbbj.backend.global.config.jwt.service.TokenService;
import com.kmbbj.backend.global.config.reponse.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final BlackListUserService blackListUserService;
    private final UserService userService;

//    private final NotificationService notificationService;

    /**
     * 유저 검색 / 리스트 조회
     *
     * @param page 첫 페이지 0 기본값
     * @param size 페이지당 유저 수
     * @param email 검색할 이메일 글자
     * @return 유저 리스트 조회 결과
     */
    @GetMapping("/user_search")
    @Operation(summary = "유저 검색 및 리스트 조회", description = "이메일을 통한 유저 검색 및 유저 리스트 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "유저 리스트 조회 성공"),
            @ApiResponse(responseCode = "404", description = "유저를 찾지 못했습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    public CustomResponse<Map<String, Object>> userListScreen(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String email) {

        Map<String, Object> result = new HashMap<>(); // 결과를 저장할 맵 초기화

            if (email != null && !email.isEmpty()) {
                // 이메일이 제공된 경우 유저 검색
                List<User> searchedUsers = adminService.searchUserByEmail(email);
                if (searchedUsers.isEmpty()) {
                    throw new ApiException(ExceptionEnum.EMAIL_NOT_FOUND); // 유저를 찾지 못한 경우 예외 발생
                }
                result.put("userList", searchedUsers);
            } else {
                // 이메일이 제공되지 않은 경우 모든 유저 리스트 조회
                Pageable pageable = PageRequest.of(page, size); // 페이지 요청 정보 생성
                Page<User> users = adminService.findAllUser(pageable); // 전체 유저 조회
                result.put("userList", users.getContent()); // 전체 유저 리스트 추가
            }

            // 응답 데이터 생성
            return new CustomResponse<>(HttpStatus.OK, "유저 리스트 조회 성공", result);
       }



    /**
     * 알람 및 로그인된 사용자 정보 조회
     *
     * @param page 첫 페이지 0 기본값
     * @param size 페이지당 보여지는 공지 수
     * @return 알림 / 사용자 정보 조회 결과
     */
    @GetMapping
    @Operation(summary = "알림 및 로그인된 사용자 정보 조회", description = "알림 리스트와 로그인된 사용자 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "알림 및 사용자 정보 조회 성공"),
            @ApiResponse(responseCode = "404", description = "알림 또는 사용자 정보를 찾지 못했습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    public CustomResponse<Map<String, Object>> getAllAlarmsAndAuthenticatedUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageRequest pageRequest = PageRequest.of(page, size);

        // 공지사항 조회
        Page<AdminAlarm> adminAlarmsPage = adminService.findAllAdminAlarm(pageRequest);

        // 공지사항의 제목과 내용 추출
        List<Map<String, String>> alarms = adminAlarmsPage.getContent().stream()
                .map(alarm -> {
                    Map<String, String> alarmData = new HashMap<>();
                    alarmData.put("title", alarm.getTitle()); // 제목 추출
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

        // 알림과 사용자 정보를 통합
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("alarms", alarms);
        responseData.put("userInfo", userInfo);

        // 응답 데이터 생성
        return new CustomResponse<>(HttpStatus.OK, "알림 및 로그인된 사용자 정보 조회 성공", responseData);
    }

    /**
     * 알림 추가 및 전송 메서드
     *
     * @param adminAlarm 알람 관련
     * @return 알림 추가 결과
     */
    @PostMapping("/add")
    @Operation(summary = "알림 추가", description = "새로운 알림을 추가합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "알림 추가 성공"),
            @ApiResponse(responseCode = "404", description = "알림 또는 사용자 정보를 찾지 못했습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    public CustomResponse<AdminAlarm> addAlarm(
            @RequestBody AdminAlarm adminAlarm) {

        Long id = adminService.getAuthenticatedUser().getId();// 로그인된 사용자 정보 가져오기

        AdminAlarm savedAlarm = adminService.saveAlarm(id, adminAlarm);// 알람 저장

        // 실시간 알림 전송 (오류로 인한 임시 주석 처리)
//        notificationService.sendNotification("/topic", savedAlarm);

        return new CustomResponse<>(HttpStatus.OK, "알림 추가 성공", savedAlarm);
    }




    /**
     * 유저 정보 조회
     *
     * @param id 사용자 ID
     * @return 사용자 정보
     */
    @GetMapping("/{id}")
    @Operation(summary = "유저 정보 조회", description = "특정 유저의 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "유저 정보 조회 성공"),
            @ApiResponse(responseCode = "404", description = "유저 정보를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    public CustomResponse<Map<String, Object>> userInformationScreen(@PathVariable Long id) {
        // 유저 정보를 가져옴
        Map<String, String> userInfo = adminService.getUser(id);

        // 결과를 합침
        Map<String, Object> result = new HashMap<>();
        result.put("userInfo", userInfo);

        return new CustomResponse<>(HttpStatus.OK, "유저 정보 조회 성공", result);
    }

    /**
     * 유저 계정 정지
     *
     * @param id 사용자 id
     * @param requestBody 정지 요청 정보
     * @return 계정 정지 결과
     */
    @PostMapping("/suspend/{id}")
    @Operation(summary = "유저 계정 정지", description = "특정 유저의 계정을 정지시킵니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "계정 정지 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청: EndDate가 필요함"),
            @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    public CustomResponse<String> suspendUser(
            @PathVariable Long id,
            @RequestBody ExampleRequestDTO requestBody) {

            LocalDateTime endDate = requestBody.getEndDate();

            // EndDate가 없으면 잘못된 요청 응답
            if (endDate == null) {
                return new CustomResponse<>(HttpStatus.BAD_REQUEST, "정지 종료 날짜가 정해지지 않았습니다", null);
            }

            blackListUserService.suspendUser(id, endDate); // 유저 엔티티 정지 날짜 삽입 / 해당 유저 토큰을 블랙리스트 토근에 저장

            Optional<User> user = userRepository.findById(id); // 아이디를 이용 사용자 조회

            if (user.isPresent()) { // 정지 날짜가 들어있는 경우(정지 성공)

                adminService.sendSuspensionEmail(user.get());// 이메일 보내기

                return new CustomResponse<>(HttpStatus.OK, "이 시간까지 사요자가 정지됩니다." + endDate, null);
            } else {
                return new CustomResponse<>(HttpStatus.NOT_FOUND, "사용자의 정지 종료 날짜를 찾지 못했습니다", null);
            }
    }

    /**
     * 유저 계정 정지 해제
     *
     * @param id 사용자 ID
     * @return 계정 정지 해제 결과
     */
    @PostMapping("/unsuspend/{id}")
    @Operation(summary = "유저 계정 정지 해제", description = "특정 유저의 계정 정지를 해제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "계정 정지 해제 성공"),
            @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    public CustomResponse<String> unsuspendUser(@PathVariable Long id) {


        Optional<User> user = userRepository.findById(id); // 아이디를 사용하여 사용자 조회

        if (user.isPresent()) { // 정지 날까가 들어있는 경우 ( 현재 정지 상태 )

            adminService.sendAccountUnblockingEmail(user.get()); // 이메일 보내기

            blackListUserService.unsuspendUser(id);// 유저 계정 정지 해제

            return new CustomResponse<>(HttpStatus.OK, "User 정지가 해제되었습니다.", null);
        } else {
            return new CustomResponse<>(HttpStatus.NOT_FOUND, "사용자의 정지 종료 날짜를 찾지 못했습니다.", null);
        }
    }
}