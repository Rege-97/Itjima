package com.itjima_server.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itjima_server.common.ApiResponseDTO;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        ApiResponseDTO<?> body = ApiResponseDTO.error(HttpServletResponse.SC_UNAUTHORIZED,
                "인증이 필요합니다. 유효한 토큰을 포함해주세요.");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
