package org.hyend.monkeymonkey.service;

import java.util.List;

public interface ScraperService {
    List<String> scrapeMealInfo(String url);
    List<String> parseMealInfo(String html);
}
