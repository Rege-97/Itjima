package com.itjima_server.controller;

import com.itjima_server.common.ApiResponseDTO;
import com.itjima_server.common.PagedResultDTO;
import com.itjima_server.dto.item.swagger.ItemPagedResponse;
import com.itjima_server.dto.notification.response.NotificationResponseDTO;
import com.itjima_server.dto.transaction.response.TransactionResponseDTO;
import com.itjima_server.security.CustomUserDetails;
import com.itjima_server.service.NotificationService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 읽지 않은 알림 리스트 조회
     *
     * @param lastId 조회할 마지막 id
     * @param size   한 페이지에 보여줄 개수
     * @param user   로그인한 사용자
     * @return 알림 리스트 응답
     */
    @Operation(
            summary = "읽지 않은 알림 리스트 조회",
            description = "읽지 않은 알림 리스트 조회 lastId와 size로 커서 기반 페이지네이션",
            responses = {
                    @ApiResponse(responseCode = "200", description = "알림 목록 조회 성공",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ItemPagedResponse.class))),
                    @ApiResponse(responseCode = "401", description = "인증 필요",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            }
    )
    @GetMapping
    public ResponseEntity<?> getNotReadList(@RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails user) {
        PagedResultDTO<?> res = notificationService.getNotReadList(user.getId(), lastId, size);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDTO.success(HttpStatus.OK.value(), "알림 목록 조회 성공", res));
    }

    /**
     * 알림 읽음 처리
     *
     * @param id   읽음 처리할 알림 ID
     * @param user 로그인한 사용자
     * @return 읽음 처리된 알림 응답
     */
    @Operation(
            summary = "알림 읽음 처리",
            description = "알림 읽음 처리",
            responses = {
                    @ApiResponse(responseCode = "200", description = "알림 읽음 처리 성공",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = TransactionResponseDTO.class))),
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
    @PutMapping("/{id}/read")
    public ResponseEntity<?> updateReadAt(@PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user) {
        NotificationResponseDTO res = notificationService.updateReadAt(id, user.getId());
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDTO.success(HttpStatus.OK.value(), "알림 읽음 처리 성공", res));
    }

}
