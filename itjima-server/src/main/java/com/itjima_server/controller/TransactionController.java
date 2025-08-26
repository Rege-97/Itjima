package com.itjima_server.controller;

import com.itjima_server.common.ApiResponseDTO;
import com.itjima_server.dto.transaction.response.TransactionResponseDTO;
import com.itjima_server.security.CustomUserDetails;
import com.itjima_server.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    public final TransactionService transactionService;

    @PutMapping("/{id}/confirm")
    public ResponseEntity<?> confirm(@PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user) {
        TransactionResponseDTO res = transactionService.confirm(id, user.getId());
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDTO.success(HttpStatus.OK.value(), "상환 요청 승인 성공", res));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user) {
        TransactionResponseDTO res = transactionService.reject(id, user.getId());
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDTO.success(HttpStatus.OK.value(), "상환 요청 거절 성공", res));
    }
}
