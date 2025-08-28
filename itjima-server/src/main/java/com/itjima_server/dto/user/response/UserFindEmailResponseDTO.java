package com.itjima_server.dto.user.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "아이디(이메일) 찾기 응답 DTO")
public class UserFindEmailResponseDTO {
    @Schema(description = "마스킹 처리된 이메일", example = "test****@example.com")
    private String maskedEmail;

    public static UserFindEmailResponseDTO from(String email) {
        // 이메일 주소의 일부를 '*'로 마스킹하는 로직
        int atIndex = email.indexOf("@");
        if (atIndex <= 4) { // @ 앞의 길이가 4 이하이면 앞 2글자만 보여줌
            return new UserFindEmailResponseDTO(email.substring(0, 2) + "****" + email.substring(atIndex));
        }
        return new UserFindEmailResponseDTO(email.substring(0, 4) + "****" + email.substring(atIndex));
    }
}
