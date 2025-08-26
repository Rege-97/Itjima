package com.itjima_server.controller;

import com.itjima_server.common.ApiResponseDTO;
import com.itjima_server.common.PagedResultDTO;
import com.itjima_server.dto.transaction.swagger.TransactionPagedResponse;
import com.itjima_server.dto.user.swagger.RecentPartnerPagedResponse;
import com.itjima_server.security.CustomUserDetails;
import com.itjima_server.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 사용자 관련 API 클래스
 *
 * @author Rege-97
 * @since 2025-08-27
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 최근 대여 사용자 목록 조회 (무한 스크롤 커서 기반)
     *
     * @param user   로그인한 사용자
     * @param lastId 마지막으로 조회한 대여 ID
     * @param size   요청한 페이지 크기
     * @return 항목(items), hasNext, lastId를 포함한 페이지 응답
     */
    @Operation(
            summary = "최근 대여 사용자 목록 조회",
            description = "최근 대여 사용자 목록 조회",
            responses = {
                    @ApiResponse(responseCode = "200", description = "상환 내역 조회 성공",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = RecentPartnerPagedResponse.class))),
                    @ApiResponse(responseCode = "401", description = "인증 필요",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            }
    )
    @GetMapping("/recent-partners")
    public ResponseEntity<?> getRecentPartnerList(@AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "20") int size) {
        PagedResultDTO<?> res = userService.getRecentPartnerList(user.getId(), lastId, size);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDTO.success(HttpStatus.OK.value(), "최근 대여 상대 목록 조회 성공", res));
    }
}
