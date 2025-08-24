package com.itjima_server.dto.user.response;

import com.itjima_server.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "사용자 기본 정보 DTO")
public class UserSimpleInfoDTO {

    @Schema(description = "사용자 ID", example = "42")
    private long id;

    @Schema(description = "사용자 이름", example = "홍길동")
    private String name;


    public static UserSimpleInfoDTO from(User user) {
        return new UserSimpleInfoDTO(user.getId(), user.getName());
    }
}