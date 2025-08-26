package com.itjima_server.controller;

import com.itjima_server.common.ApiResponseDTO;
import com.itjima_server.dto.agreement.response.AgreementResponseDTO;
import com.itjima_server.dto.transaction.response.TransactionResponseDTO;
import com.itjima_server.security.CustomUserDetails;
import com.itjima_server.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 상환 관련 API 클래스
 *
 * @author Rege-97
 * @since 2025-08-26
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    public final TransactionService transactionService;

    /**
     * 상환 요청 승인 처리(채권자만 가능)
     *
     * @param id   상환 ID
     * @param user 로그인한 유저
     * @return 상환 승인 응답
     */
    @Operation(
            summary = "상환 요청 승인",
            description = "상환 요청 승인 처리(채권자만 가능)",
            responses = {
                    @ApiResponse(responseCode = "201", description = "상환 요청 승인 성공",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = TransactionResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "요청 검증 실패",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                    @ApiResponse(responseCode = "401", description = "인증 필요",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                    @ApiResponse(responseCode = "403", description = "권한 없음",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                    @ApiResponse(responseCode = "404", description = "대상 없음",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                    @ApiResponse(responseCode = "409", description = "요청 불가 상태",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            }
    )
    @PutMapping("/{id}/confirm")
    public ResponseEntity<?> confirm(@PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user) {
        TransactionResponseDTO res = transactionService.confirm(id, user.getId());
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDTO.success(HttpStatus.OK.value(), "상환 요청 승인 성공", res));
    }

    /**
     * 상환 요청 거절 처리(채권자만 가능)
     *
     * @param id   상환 ID
     * @param user 로그인한 유저
     * @return 상환 거절 응답
     */
    @Operation(
            summary = "상환 요청 거절",
            description = "상환 요청 거절 처리(채권자만 가능)",
            responses = {
                    @ApiResponse(responseCode = "201", description = "상환 요청 거절 성공",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = TransactionResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "요청 검증 실패",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                    @ApiResponse(responseCode = "401", description = "인증 필요",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                    @ApiResponse(responseCode = "403", description = "권한 없음",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                    @ApiResponse(responseCode = "404", description = "대상 없음",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                    @ApiResponse(responseCode = "409", description = "요청 불가 상태",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            }
    )
    @PutMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user) {
        TransactionResponseDTO res = transactionService.reject(id, user.getId());
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDTO.success(HttpStatus.OK.value(), "상환 요청 거절 성공", res));
    }
}
