package com.itjima_server.controller;

import com.itjima_server.common.ApiResponseDTO;
import com.itjima_server.common.PagedResultDTO;
import com.itjima_server.dto.agreement.swagger.AgreementPagedResponse;
import com.itjima_server.dto.dashboard.response.DashboardResponseDTO;
import com.itjima_server.security.CustomUserDetails;
import com.itjima_server.service.DashboardService;
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
 * 대쉬보드 관련 API 클래스
 *
 * @author Rege-97
 * @since 2025-09-10
 */
@RestController
@RequestMapping("/api/dashboards")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 화면 렌더링용 대쉬보드
     *
     * @param user 로그인한 사용자
     * @return 대여 리스트 응답 DTO
     */
    @Operation(
            summary = "화면 렌더링용 대쉬보드",
            description = "대여 횟수, 반납 임박, 연체 대여 조회",
            responses = {
                    @ApiResponse(responseCode = "200", description = "대여 목록 조회 성공",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = DashboardResponseDTO.class))),
                    @ApiResponse(responseCode = "401", description = "인증 필요",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            }
    )
    @GetMapping("/summary")
    public ResponseEntity<?> getSummaries(@AuthenticationPrincipal CustomUserDetails user) {
        DashboardResponseDTO res = dashboardService.getDashboardInfo(user.getId());
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDTO.success(HttpStatus.OK.value(), "대쉬보드 조회 성공", res));
    }

    /**
     * 요청 목록 조회 (무한 스크롤 커서 기반)
     *
     * @param lastId 조회할 마지막 id
     * @param size   한 페이지에 보여줄 개수
     * @param user   로그인한 유저
     * @return 항목(items), hasNext, lastId를 포함한 페이지 응답
     */
    @Operation(
            summary = "요청 목록 조회(커서 기반)",
            description = "lastId와 size로 커서 기반 페이지네이션",
            responses = {
                    @ApiResponse(responseCode = "200", description = "요청 목록 조회 성공",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = AgreementPagedResponse.class))),
                    @ApiResponse(responseCode = "401", description = "인증 필요",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            }
    )
    @GetMapping("/pending")
    public ResponseEntity<?> getPending(@RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "5") int size,
            @AuthenticationPrincipal CustomUserDetails user) {
        PagedResultDTO<?> res = dashboardService.getPending(user.getId(), lastId, size);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDTO.success(HttpStatus.OK.value(), "요청 목록 조회 성공", res));
    }

}
