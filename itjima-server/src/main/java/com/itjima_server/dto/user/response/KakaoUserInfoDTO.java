package com.itjima_server.dto.user.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoUserInfoDTO {

    private String id;

    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    private Properties properties;

    @Getter
    @Setter
    public static class KakaoAccount {
        private String email;
    }

    @Getter
    @Setter
    public static class Properties {
        private String nickname;
    }
}