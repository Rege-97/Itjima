package com.itjima_server.dto.user.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "카카오 OAuth2 토큰 응답 DTO")
public class KakaoTokenResponseDTO {

    @Schema(description = "액세스 토큰", example = "access-token-xxxxxxxxxx")
    @JsonProperty("access_token")
    private String accessToken;

    @Schema(description = "토큰 타입", example = "bearer")
    @JsonProperty("token_type")
    private String tokenType;

    @Schema(description = "리프레시 토큰", example = "refresh-token-xxxxxxxxxx")
    @JsonProperty("refresh_token")
    private String refreshToken;

    @Schema(description = "액세스 토큰 만료(초)", example = "21599")
    @JsonProperty("expires_in")
    private Integer expiresIn;

    @Schema(description = "부여된 스코프(공백 구분)", example = "account_email profile_nickname")
    @JsonProperty("scope")
    private String scope;

    @Schema(description = "리프레시 토큰 만료(초)", example = "5183999")
    @JsonProperty("refresh_token_expires_in")
    private Integer refreshTokenExpiresIn;
}