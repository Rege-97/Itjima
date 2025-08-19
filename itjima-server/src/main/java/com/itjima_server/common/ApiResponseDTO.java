package com.itjima_server.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponseDTO<T> {

    private int status;
    private String message;
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
