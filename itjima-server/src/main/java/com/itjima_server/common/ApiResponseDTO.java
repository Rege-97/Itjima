package com.itjima_server.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "API 공통 응답 DTO")
public class ApiResponseDTO<T> {

    @Schema(description = "HTTP 상태 코드", example = "200")
    private int status;

    @Schema(description = "응답 메시지", example = "회원 가입 성공")
    private String message;

    @Schema(description = "응답 데이터")
    private T data;

    // 성공 응답
    public static <T> ApiResponseDTO<T> success(int status, String message, T data) {
        return new ApiResponseDTO<>(status, message, data);
    }

    // 실패 응답
    public static <T> ApiResponseDTO<T> error(int status, String message) {
        return new ApiResponseDTO<>(status, message, null);
    }
}
