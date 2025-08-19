package com.itjima_server.domain;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class User {

    private long id;
    private String name;
    private String email;
    private String password;
    private String phone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Provider provider;
    private String providerId;
}
