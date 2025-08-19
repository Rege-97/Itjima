package com.itjima_server.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserLoginResponseDTO {

    private long id;
    private String name;
    private String email;
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;

}
