package com.kmbbj.backend.admin.controller;

import com.kmbbj.backend.admin.dto.RewardRequest;
import com.kmbbj.backend.admin.service.RewardService;
import com.kmbbj.backend.balance.entity.ChangeType;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;

import com.kmbbj.backend.global.config.reponse.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("admin/rewards")
@RequiredArgsConstructor
public class RewardController {

    private final RewardService rewardService;

    /**
     * 특정 유저에게 보상 지급
     *
     * @param userId  유저 ID
     * @param request 보상 요청 데이터 (보상 금액 포함)
     * @return 보상 지급 결과
     */
    @PostMapping("/{userId}")
    @Operation(summary = "유저 보상 지급", description = "특정 유저에게 보상을 지급합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "보상 지급 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (보상 금액이 0 이하)"),
            @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    public CustomResponse<String> rewardUser(
            @PathVariable Long userId,
            @RequestBody RewardRequest request) {

        try {
            // 보상 금액이 유효한지 확인
            if (request.getAmount() <= 0) {
                throw new ApiException(ExceptionEnum.INVALID_AMOUNT); // 보상 금액이 0보다 작을 경우
            }
            rewardService.rewardUser(userId, request.getAmount(), ChangeType.BONUS); // 보상 지급 서비스

            return new CustomResponse<>(HttpStatus.OK, "보상 지급 성공", null); // 성공시

        } catch (Exception e) {
            return new CustomResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류 발생", null);
        }
    }
}
