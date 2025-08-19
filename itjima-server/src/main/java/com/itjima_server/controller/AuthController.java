package com.itjima_server.controller;

import com.itjima_server.common.ApiResponseDTO;
import com.itjima_server.dto.request.UserLoginRequestDTO;
import com.itjima_server.dto.request.UserRegisterRequestDTO;
import com.itjima_server.dto.response.UserLoginResponseDTO;
import com.itjima_server.dto.response.UserResponseDTO;
import com.itjima_server.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증/회원가입 API 클래스
 *
 * @author Rege-97
 * @since 2025-08-19
 */
@Tag(name = "Auth", description = "인증/회원가입 API")
@RestController
@RequestMapping("/api/auth/")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;


    /**
     * 신규 사용자 회원가입
     *
     * @param req 회원 가입 요청 DTO
     * @return 회원가입 결과 응답
     */
    @Operation(summary = "회원가입", description = "신규 사용자 등록")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원 가입 성공", content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "409", description = "이메일/전화번호 중복"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/signup")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegisterRequestDTO req) {
        UserResponseDTO res = userService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(HttpStatus.CREATED.value(), "회원 가입 성공", res));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginRequestDTO req) {
        UserLoginResponseDTO res = userService.login(req);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDTO.success(HttpStatus.OK.value(), "로그인 성공", res));
    }
}
