package com.itjima_server.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TokenResponseDTO {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;

}
