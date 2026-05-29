package org.hyend.monkeymonkey.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hyend.monkeymonkey.dto.KakaoToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoMessageServiceImpl implements KakaoMessageService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Setter
    private String tokenFilePath = "kakao-token.json";

    @Value("${kakao.client-id:}")
    private String clientId;

    @Override
    public void sendToMe(String message) {
        KakaoToken token = loadToken();
        if (token == null) {
            log.error("Kakao token not found. Please initialize kakao-token.json");
            return;
        }

        String url = "https://kapi.kakao.com/v2/api/talk/memo/default/send";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(token.getAccessToken());

        String templateObject;
        try {
            var linkMap = java.util.Map.of(
                    "web_url", "https://www.hanyang.ac.kr/web/www/re15",
                    "mobile_web_url", "https://www.hanyang.ac.kr/web/www/re15"
            );
            var templateMap = java.util.Map.of(
                    "object_type", "text",
                    "text", message,
                    "link", linkMap,
                    "button_title", "식단 보기"
            );
            templateObject = objectMapper.writeValueAsString(templateMap);
        } catch (Exception e) {
            log.error("Failed to create template object", e);
            return;
        }

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("template_object", templateObject);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Message sent successfully");
            } else {
                log.error("Failed to send message: {}", response.getBody());
            }
        } catch (Exception e) {
            log.error("Error while sending kakao message", e);
            // 만약 401 Unauthorized 면 토큰 갱신 시도 로직이 필요할 수 있음
        }
    }

    @Override
    public KakaoToken loadToken() {
        File file = new File(tokenFilePath);
        if (!file.exists()) {
            return null;
        }
        try {
            return objectMapper.readValue(file, KakaoToken.class);
        } catch (IOException e) {
            log.error("Failed to load kakao token from file", e);
            return null;
        }
    }

    @Override
    public void saveToken(KakaoToken token) {
        try {
            objectMapper.writeValue(new File(tokenFilePath), token);
        } catch (IOException e) {
            log.error("Failed to save kakao token to file", e);
        }
    }

    @Override
    public boolean refreshToken() {
        KakaoToken token = loadToken();
        if (token == null || token.getRefreshToken() == null) {
            log.error("Refresh token not found. Please check kakao-token.json");
            return false;
        }

        if (clientId == null || clientId.isEmpty()) {
            log.error("Kakao Client ID (REST API KEY) is missing in application.properties");
            return false;
        }

        String url = "https://kauth.kakao.com/oauth/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "refresh_token");
        params.add("client_id", clientId);
        params.add("refresh_token", token.getRefreshToken());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<KakaoToken> response = restTemplate.postForEntity(url, request, KakaoToken.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                KakaoToken newToken = response.getBody();
                if (newToken.getRefreshToken() == null) {
                    newToken.setRefreshToken(token.getRefreshToken());
                }
                saveToken(newToken);
                log.info("Token refreshed and saved successfully");
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Failed to refresh kakao token", e);
            return false;
        }
    }
}
