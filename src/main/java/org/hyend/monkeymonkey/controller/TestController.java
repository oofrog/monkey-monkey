package org.hyend.monkeymonkey.controller;

import lombok.RequiredArgsConstructor;
import org.hyend.monkeymonkey.service.SchedulerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final SchedulerService schedulerService;

    @GetMapping("/test-meal")
    public String testMeal() {
        try {
            schedulerService.processMealInfo();
            return "✅ 식단 정보 처리 프로세스가 수동으로 실행되었습니다. 로그와 카카오톡을 확인하세요!";
        } catch (Exception e) {
            return "❌ 오류 발생: " + e.getMessage();
        }
    }
}
