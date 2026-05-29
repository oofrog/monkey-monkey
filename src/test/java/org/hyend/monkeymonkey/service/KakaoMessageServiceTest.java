package org.hyend.monkeymonkey.service;

import org.hyend.monkeymonkey.dto.KakaoToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class KakaoMessageServiceTest {

    private KakaoMessageServiceImpl kakaoMessageService;
    private RestTemplate restTemplate;

    @TempDir
    Path tempDir;

    private String tokenFilePath;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        tokenFilePath = tempDir.resolve("kakao-token.json").toString();
        kakaoMessageService = new KakaoMessageServiceImpl(restTemplate);
        // We need to inject the file path into the service for testing
        kakaoMessageService.setTokenFilePath(tokenFilePath);
    }

    @Test
    @DisplayName("토큰 정보를 파일에 저장하고 다시 읽어올 수 있어야 한다")
    void shouldSaveAndLoadToken() {
        // given
        KakaoToken token = KakaoToken.builder()
                .accessToken("test-access-token")
                .refreshToken("test-refresh-token")
                .build();

        // when
        kakaoMessageService.saveToken(token);
        KakaoToken loadedToken = kakaoMessageService.loadToken();

        // then
        assertThat(loadedToken).isNotNull();
        assertThat(loadedToken.getAccessToken()).isEqualTo("test-access-token");
        assertThat(loadedToken.getRefreshToken()).isEqualTo("test-refresh-token");
    }

    @Test
    @DisplayName("파일이 없는 경우 null을 반환해야 한다")
    void shouldReturnNullWhenFileNotExists() {
        // when
        KakaoToken loadedToken = kakaoMessageService.loadToken();

        // then
        assertThat(loadedToken).isNull();
    }
}
