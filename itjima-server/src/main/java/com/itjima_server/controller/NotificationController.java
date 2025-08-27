package com.itjima_server.controller;

import com.itjima_server.common.ApiResponseDTO;
import com.itjima_server.common.PagedResultDTO;
import com.itjima_server.dto.notification.response.NotificationResponseDTO;
import com.itjima_server.security.CustomUserDetails;
import com.itjima_server.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    @GetMapping
    public ResponseEntity<?> getNotReadList(@RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails user) {
        PagedResultDTO<?> res = notificationService.getNotReadList(user.getId(), lastId, size);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDTO.success(HttpStatus.OK.value(), "알림 목록 조회 성공", res));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> updateReadAt(@PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user) {
        NotificationResponseDTO res = notificationService.updateReadAt(id, user.getId());
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDTO.success(HttpStatus.OK.value(), "알림 읽음 처리 성공", res));
    }

}
