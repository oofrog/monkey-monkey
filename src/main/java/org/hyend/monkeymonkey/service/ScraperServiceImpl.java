package org.hyend.monkeymonkey.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ScraperServiceImpl implements ScraperService {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";

    @Override
    public List<String> scrapeMealInfo(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(10000)
                    .get();
            return parseMealInfo(doc.html());
        } catch (IOException e) {
            log.error("Failed to scrape meal info from {}", url, e);
            return List.of("식단 정보를 가져오는 중 오류가 발생했습니다.");
        }
    }

    @Override
    public List<String> parseMealInfo(String html) {
        Document doc = Jsoup.parse(html);
        List<String> meals = new ArrayList<>();

        // "오늘의 메뉴" (Daily View) 영역만 타겟팅
        // h3.hyu-element 태그 중 "중식" 텍스트를 가진 요소를 찾고, 바로 다음에 오는 식단 컨테이너에서 메뉴 추출
        Elements lunchHeaders = doc.select(".hyu-list-container-dailyView h3.hyu-element:contains(중식)");
        
        for (Element header : lunchHeaders) {
            // 중식 헤더 바로 다음 형제 요소인 hyu-list-container를 찾음
            Element menuContainer = header.nextElementSibling();
            if (menuContainer != null && menuContainer.hasClass("hyu-list-container")) {
                Elements menuParagraphs = menuContainer.select(".menu-detail p");
                for (Element p : menuParagraphs) {
                    String text = p.text().trim();
                    if (isValidMenu(text) && !meals.contains(text)) {
                        meals.add(text);
                    }
                }
            }
        }

        return meals;
    }

    private boolean isValidMenu(String text) {
        // 메뉴 이름으로 보기 힘든 짧은 글자나 시간 형식 제외
        return text.length() > 2 && !text.contains("~") && !text.contains(":");
    }
}
