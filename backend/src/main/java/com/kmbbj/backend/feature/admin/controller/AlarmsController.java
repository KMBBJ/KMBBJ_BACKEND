package com.kmbbj.backend.feature.admin.controller;

import com.kmbbj.backend.feature.admin.entity.AdminAlarm;
import com.kmbbj.backend.feature.admin.service.AdminService;
import com.kmbbj.backend.global.config.reponse.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController("/")
@RequiredArgsConstructor
public class AlarmsController {
    private final AdminService adminService;

    /**
     * 알람 조회
     *
     * @param page 첫 페이지 0 기본값
     * @param size 페이지당 보여지는 공지 수
     * @return 알림 정보 조회 결과
     */
    @GetMapping("/announcements")
    @Operation(summary = "알림 조회", description = "공지사항을 페이징하여 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "알림 정보 조회 성공"),
            @ApiResponse(responseCode = "404", description = "알림 정보를 찾지 못했습니다."),
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

        // 알림 정보를 통합
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("alarms", alarms);

        // 응답 데이터 생성
        return new CustomResponse<>(HttpStatus.OK, "알림 정보 조회 성공", responseData);
    }
}
