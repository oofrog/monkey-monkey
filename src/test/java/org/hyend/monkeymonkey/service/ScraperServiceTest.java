package org.hyend.monkeymonkey.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScraperServiceTest {

    private final ScraperService scraperService = new ScraperServiceImpl();

    @Test
    @DisplayName("HTML에서 중식 메뉴만 정확히 추출하고 석식은 제외해야 한다")
    void shouldExtractOnlyLunchInfoFromHtml() {
        // given
        String mockHtml = """
                <div class="hyu-list-container-dailyView">
                    <h3 class="hyu-element">중식</h3>
                    <div class="hyu-list-container">
                        <div class="menu-detail">
                            <p>김치찌개, 불고기</p>
                        </div>
                    </div>
                    <h3 class="hyu-element">석식</h3>
                    <div class="hyu-list-container">
                        <div class="menu-detail">
                            <p>된장찌개, 제육볶음</p>
                        </div>
                    </div>
                </div>
                """;

        // when
        List<String> meals = scraperService.parseMealInfo(mockHtml);

        // then
        assertThat(meals).hasSize(1);
        assertThat(meals.get(0)).contains("김치찌개");
        assertThat(meals).noneMatch(m -> m.contains("된장찌개"));
    }

    @Test
    @DisplayName("식단 정보가 없는 경우 빈 리스트를 반환해야 한다")
    void shouldReturnEmptyListWhenNoMealInfo() {
        // given
        String emptyHtml = "<div>식단 정보가 없습니다.</div>";

        // when
        List<String> meals = scraperService.parseMealInfo(emptyHtml);

        // then
        assertThat(meals).isEmpty();
    }
}
