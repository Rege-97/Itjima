package com.itjima_server.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.itjima_server.domain.Provider;
import com.itjima_server.domain.User;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponseDTO {

    private long id;
    private String name;
    private String email;
    private String phone;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    private Provider provider;
    private String providerId;

    public static UserResponseDTO from(User user) {
        return new UserResponseDTO(user.getId(), user.getName(), user.getEmail(), user.getPhone(),
                user.getCreatedAt(), user.getProvider(), user.getProviderId());
    }
}
