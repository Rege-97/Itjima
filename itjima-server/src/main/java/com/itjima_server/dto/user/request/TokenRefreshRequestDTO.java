package com.itjima_server.dto.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "토큰 리프레쉬 요청 DTO")
public class TokenRefreshRequestDTO {

    @Schema(description = "리프레쉬 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
    @NotBlank(message = "리프레쉬 토큰은 필수입니다.")
    private String refreshToken;
}
