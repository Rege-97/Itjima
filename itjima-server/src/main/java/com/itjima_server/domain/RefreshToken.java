package com.itjima_server.domain;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RefreshToken {

    private long id;
    private long userId;
    private String token;
    private LocalDateTime expiryDate;
}
