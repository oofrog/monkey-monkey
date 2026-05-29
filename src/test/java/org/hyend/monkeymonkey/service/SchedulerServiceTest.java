package org.hyend.monkeymonkey.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchedulerServiceTest {

    @Mock
    private ScraperService scraperService;

    @Mock
    private KakaoMessageService kakaoMessageService;

    @InjectMocks
    private SchedulerServiceImpl schedulerService;

    @Test
    @DisplayName("스케줄러는 두 식당의 중식을 가져와서 하나의 메시지로 전송해야 한다")
    void shouldScrapeAndSendMultiMealInfo() {
        // given
        when(kakaoMessageService.refreshToken()).thenReturn(true);
        when(scraperService.scrapeMealInfo(anyString()))
                .thenReturn(List.of("김치찌개"))
                .thenReturn(List.of("불고기"));

        // when
        schedulerService.processMealInfo();

        // then
        verify(kakaoMessageService, times(1)).refreshToken();
        verify(scraperService, times(2)).scrapeMealInfo(anyString());
        verify(kakaoMessageService, times(1)).sendToMe(argThat(msg -> 
            msg.contains("창업보육센터") && msg.contains("김치찌개") &&
            msg.contains("교직원식당") && msg.contains("불고기")
        ));
    }

    @Test
    @DisplayName("식단 정보가 모두 없는 경우에도 제목과 함께 정보를 전송해야 한다")
    void shouldHandleEmptyMealInfo() {
        // given
        when(kakaoMessageService.refreshToken()).thenReturn(true);
        when(scraperService.scrapeMealInfo(anyString())).thenReturn(List.of());

        // when
        schedulerService.processMealInfo();

        // then
        verify(kakaoMessageService, times(1)).sendToMe(contains("식단 정보가 없습니다"));
    }

    @Test
    @DisplayName("토큰 갱신에 실패하면 프로세스를 중단해야 한다")
    void shouldAbortWhenTokenRefreshFails() {
        // given
        when(kakaoMessageService.refreshToken()).thenReturn(false);

        // when
        schedulerService.processMealInfo();

        // then
        verify(scraperService, never()).scrapeMealInfo(anyString());
        verify(kakaoMessageService, never()).sendToMe(anyString());
    }
}
