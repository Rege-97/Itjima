package com.itjima_server.controller;

import com.itjima_server.common.ApiResponseDTO;
import com.itjima_server.dto.request.TokenRefreshRequestDTO;
import com.itjima_server.dto.request.UserLoginRequestDTO;
import com.itjima_server.dto.request.UserRegisterRequestDTO;
import com.itjima_server.dto.response.TokenResponseDTO;
import com.itjima_server.dto.response.UserLoginResponseDTO;
import com.itjima_server.dto.response.UserResponseDTO;
import com.itjima_server.security.CustomUserDetails;
import com.itjima_server.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증/회원가입 API 클래스
 *
 * @author Rege-97
 * @since 2025-08-20
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
    @Operation(
            summary = "회원가입",
            description = "신규 사용자 등록"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "요청 검증 실패",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "409", description = "중복 이메일/전화번호",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @PostMapping("/signup")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegisterRequestDTO req) {
        UserResponseDTO res = userService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(HttpStatus.CREATED.value(), "회원 가입 성공", res));
    }

    /**
     * 회원 로그인
     *
     * @param req 로그인 요청 DTO
     * @return 로그인 결과 응답
     */
    @Operation(
            summary = "로그인",
            description = "액세스/리프레시 토큰 발급"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserLoginResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "요청 검증 실패",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginRequestDTO req) {
        UserLoginResponseDTO res = userService.login(req);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDTO.success(HttpStatus.OK.value(), "로그인 성공", res));
    }

    /**
     * 액세스 토큰 재발급
     *
     * @param req 리프레쉬 토큰 DTO
     * @return 재발급 결과 응답
     */
    @Operation(
            summary = "토큰 재발급",
            description = "유효한 리프레시 토큰으로 액세스 토큰 재발급"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재발급 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TokenResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "요청 검증 실패",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "401", description = "리프레시 토큰 무효/만료",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@Valid @RequestBody TokenRefreshRequestDTO req) {
        TokenResponseDTO res = userService.refreshAccessToken(req);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDTO.success(HttpStatus.OK.value(), "토큰 재발급 성공", res));
    }

    /**
     * 로그아웃
     *
     * @param user 토큰 인증된 사용자
     * @return 로그아웃 결과 메세지
     */
    @Operation(
            summary = "로그아웃",
            description = "리프레시 토큰 폐기",
            security = {@SecurityRequirement(name = "bearerAuth")} // Swagger UI의 Authorize 버튼과 연동
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal CustomUserDetails user) {
        userService.logout(user.getId());
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDTO.success(HttpStatus.OK.value(), "로그아웃 성공"));
    }
}
