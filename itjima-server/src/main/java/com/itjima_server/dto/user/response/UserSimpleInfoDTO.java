package com.itjima_server.dto.user.response;

import com.itjima_server.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserSimpleInfoDTO {

    private Long id;
    private String name;

    public static UserSimpleInfoDTO from(User user) {
        return new UserSimpleInfoDTO(user.getId(), user.getName());
    }
}