package com.itjima_server.dto.user.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "휴대폰 번호 검색 응답 DTO")
public class UserSearchResponseDTO {

    @Schema(description = "사용자 ID", example = "1")
    private long id;

    @Schema(description = "이름", example = "홍길동")
    private String name;

    @Schema(description = "이메일", example = "honggildong@example.com")
    private String email;

    @Schema(description = "전화번호", example = "01012345678")
    private String phone;
}
