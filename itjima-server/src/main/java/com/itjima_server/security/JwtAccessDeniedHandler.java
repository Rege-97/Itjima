package com.itjima_server.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itjima_server.common.ApiResponseDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        ApiResponseDTO<?> body = ApiResponseDTO.error(HttpStatus.FORBIDDEN.value(),
                "접근 권한이 없습니다.");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
