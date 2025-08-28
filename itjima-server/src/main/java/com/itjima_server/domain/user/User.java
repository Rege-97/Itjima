package com.itjima_server.domain.user;

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
    private UserStatus status;
    private LocalDateTime deletedAt;
    private boolean emailVerified;
    private String emailVerificationToken;
    private LocalDateTime emailTokenGeneratedAt;
    private String passwordResetToken;
    private LocalDateTime passwordTokenGeneratedAt;
}
