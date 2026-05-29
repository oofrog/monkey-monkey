package org.hyend.monkeymonkey.service;

import org.hyend.monkeymonkey.dto.KakaoToken;

public interface KakaoMessageService {
    void sendToMe(String message);
    KakaoToken loadToken();
    void saveToken(KakaoToken token);
    boolean refreshToken();
}
