package com.itjima_server.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenRefreshRequestDTO {

    @NotBlank(message = "리프레쉬 토큰은 필수입니다.")
    private String refreshToken;
}
