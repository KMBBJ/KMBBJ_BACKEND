package com.kmbbj.backend.global.sse;

import com.kmbbj.backend.feature.admin.entity.AdminAlarm;
import com.kmbbj.backend.feature.admin.service.AdminService;
import com.kmbbj.backend.global.config.reponse.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class SseController {

    private final SseService sseService;
    private final AdminService adminService;

    @GetMapping(value = "/api/sse/subscribe/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable Long userId) {
        return sseService.createEmitter(userId);
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


        AdminDTO adminDTO = new AdminDTO();
        adminDTO.setTitle(adminAlarm.getTitle()); // AdminAlarm에서 제목 추출
        adminDTO.setContent(adminAlarm.getContent()); // AdminAlarm에서 내용 추출

        sseService.sendAdminNotification(id, adminDTO); // 공지사항 전송

        return new CustomResponse<>(HttpStatus.OK, "알림 추가 성공", savedAlarm);
    }

}
