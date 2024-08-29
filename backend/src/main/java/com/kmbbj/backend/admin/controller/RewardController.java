package com.kmbbj.backend.admin.controller;

import com.kmbbj.backend.admin.dto.RewardRequest;
import com.kmbbj.backend.admin.service.RewardService;
import com.kmbbj.backend.balance.entity.ChangeType;
import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import com.kmbbj.backend.global.config.reponse.CustomResponse;
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
     * @param userId     유저 id
     * @param
     * @param
     * @return 보상 지급 결과
     */
    @PostMapping("/{userId}")
    public CustomResponse<String> rewardUser(
            @PathVariable Long userId,
            @RequestBody RewardRequest request) {  // 변경된 부분: @RequestBody 사용

        try {
            if (request.getAmount() <= 0) {
                throw new ApiException(ExceptionEnum.INVALID_AMOUNT);
            }
            System.out.println(userId);

            rewardService.rewardUser(userId, request.getAmount(), ChangeType.valueOf("BONUS"));

            return new CustomResponse<>(HttpStatus.OK, "보상 지급 성공", null);

        } catch (ApiException e) {
            return new CustomResponse<>(e.getException().getStatus(), e.getMessage(), null);
        } catch (Exception e) {
            return new CustomResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류 발생", null);
        }
    }
}