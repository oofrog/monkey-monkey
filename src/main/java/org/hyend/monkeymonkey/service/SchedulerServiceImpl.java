package org.hyend.monkeymonkey.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerServiceImpl implements SchedulerService {

    private final ScraperService scraperService;
    private final KakaoMessageService kakaoMessageService;

    private static final String BUSINESS_INCUBATION_URL = "https://www.hanyang.ac.kr/web/www/re15";
    private static final String FACULTY_CAFE_URL = "https://www.hanyang.ac.kr/web/www/re11";

    @Override
    @Scheduled(cron = "0 0 8 * 3-6,9-12 1-5", zone = "Asia/Seoul")
    public void processMealInfo() {
        log.info("Starting scheduled meal info process...");
        
        // 1. 토큰 갱신
        if (!kakaoMessageService.refreshToken()) {
            log.error("Aborting meal info process due to token refresh failure.");
            return;
        }

        // 2. 식단 스크래핑 (중식만)
        List<String> businessMeals = scraperService.scrapeMealInfo(BUSINESS_INCUBATION_URL);
        List<String> facultyMeals = scraperService.scrapeMealInfo(FACULTY_CAFE_URL);

        // 3. 메시지 조립
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("🍱 오늘의 중식 리스트\n\n");
        
        appendCafeteriaInfo(messageBuilder, "🏢 창업보육센터", businessMeals);
        messageBuilder.append("\n------------------\n\n");
        appendCafeteriaInfo(messageBuilder, "👩‍🏫 교직원식당", facultyMeals);

        kakaoMessageService.sendToMe(messageBuilder.toString().trim());
        log.info("Scheduled meal info process completed.");
    }

    private void appendCafeteriaInfo(StringBuilder sb, String title, List<String> meals) {
        sb.append(title).append("\n");
        if (meals == null || meals.isEmpty()) {
            sb.append("- 오늘은 식단 정보가 없습니다.\n");
        } else {
            for (String meal : meals) {
                sb.append("- ").append(meal).append("\n");
            }
        }
    }
}
