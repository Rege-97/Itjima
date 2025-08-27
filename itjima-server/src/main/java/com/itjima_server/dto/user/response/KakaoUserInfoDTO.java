package com.itjima_server.dto.user.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "카카오 사용자 정보 응답 DTO")
public class KakaoUserInfoDTO {

    @Schema(description = "카카오 사용자 고유 식별자", example = "1234567890")
    private String id;

    @Schema(description = "카카오 계정 상세 정보")
    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    @Schema(description = "카카오 프로필 정보")
    private Properties properties;

    @Getter
    @Setter
    @Schema(description = "카카오 계정 상세 필드")
    public static class KakaoAccount {

        @Schema(description = "이메일 주소", example = "example@example.com")
        private String email;
    }

    @Getter
    @Setter
    @Schema(description = "카카오 프로필 정보")
    public static class Properties {

        @Schema(description = "닉네임", example = "길동")
        private String nickname;
    }
}