package com.itjima_server.dto.user.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.itjima_server.domain.Provider;
import com.itjima_server.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "회원 응답 DTO")
public class UserResponseDTO {

    @Schema(description = "사용자 ID", example = "1")
    private long id;

    @Schema(description = "이름", example = "홍길동")
    private String name;

    @Schema(description = "이메일", example = "honggildong@example.com")
    private String email;

    @Schema(description = "전화번호", example = "01012345678")
    private String phone;

    @Schema(description = "가입일시", example = "2025-08-19 16:00:23")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "가입 경로", example = "LOCAL")
    private Provider provider;

    @Schema(description = "소셜 로그인 Provider ID (LOCAL이면 null)", example = "null")
    private String providerId;

    public static UserResponseDTO from(User user) {
        return new UserResponseDTO(user.getId(), user.getName(), user.getEmail(), user.getPhone(),
                user.getCreatedAt(), user.getProvider(), user.getProviderId());
    }
}
