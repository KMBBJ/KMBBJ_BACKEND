package com.kmbbj.backend.matching.controller;

import com.kmbbj.backend.auth.entity.User;
import com.kmbbj.backend.global.config.reponse.CustomResponse;
import com.kmbbj.backend.global.config.security.FindUserBySecurity;
import com.kmbbj.backend.matching.service.matching.MatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/matching")
@RequiredArgsConstructor
public class MatchingController {
    private final MatchingService matchingService;
//    @PostMapping("/start/quick")
//    public CustomResponse<Void> startQuickEntry() {
//        matchingService.startQuickEntry();
//        return new CustomResponse<>(HttpStatus.OK, "빠른 매칭 성공", null);
//    }

    @PostMapping("/start/random")
    public CustomResponse<Void> startRandomMatching() {
        System.out.println("아직 안돔");
        matchingService.startRandomMatching();
        System.out.println("돌았음");
        return new CustomResponse<>(HttpStatus.OK, "랜덤 매칭 요청 성공", null);
    }

    @PostMapping("/cancel")
    public CustomResponse<Void> cancelMatching() {
        matchingService.cancelCurrentUserScheduledTasks();
        return new CustomResponse<>(HttpStatus.OK, "대기열 취소", null);
    }
}
