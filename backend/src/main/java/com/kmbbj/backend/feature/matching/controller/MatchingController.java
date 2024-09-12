package com.kmbbj.backend.feature.matching.controller;

import com.kmbbj.backend.global.config.reponse.CustomResponse;
import com.kmbbj.backend.global.config.reponse.ErrorResponse;
import com.kmbbj.backend.global.config.security.FindUserBySecurity;
import com.kmbbj.backend.feature.matching.service.matching.MatchingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/matching")
@RequiredArgsConstructor
@Tag(name = "Matching", description = "랜덤 매칭 API")
public class MatchingController {
    private final MatchingService matchingService;
    private final FindUserBySecurity findUserBySecurity;

    /**
     * 랜덤 매칭
     * @return  CustomResponse  응답(OK,"랜덤 매칭 요청 성공",null)
     */
    @Operation(summary = "랜덤 매칭 시작", description = "자산이 비슷한 유저끼리 매칭")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "매칭 요청 성공"),
            @ApiResponse(responseCode = "404", description = "유저/방/자산을 찾지 못했습니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "이미 다른 방에 입장해 있습니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "내부 서버 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/start/random")
    public CustomResponse<Void> startRandomMatching() {
        matchingService.startRandomMatching();
        return new CustomResponse<>(HttpStatus.OK, "매칭 요청 성공", null);
    }

    /**
     * 매칭 취소
     * @return  CustomResponse  응답(OK,"대기열 취소",null)
     */
    @Operation(summary = "매칭 취소", description = "랜덤 매칭 취소")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "대기열 취소"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "방을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/cancel")
    public CustomResponse<Void> cancelMatching() {
        matchingService.cancelCurrentUserScheduledTasks();
        matchingService.cancelMatching(findUserBySecurity.getCurrentUser());
        return new CustomResponse<>(HttpStatus.OK, "대기열 취소", null);
    }
}
