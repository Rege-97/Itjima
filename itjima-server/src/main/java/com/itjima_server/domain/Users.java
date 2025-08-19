package com.itjima_server.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Users {
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
