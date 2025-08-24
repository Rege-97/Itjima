package com.itjima_server.controller;

import com.itjima_server.common.ApiResponseDTO;
import com.itjima_server.dto.agreement.request.AgreementCreateRequestDTO;
import com.itjima_server.dto.agreement.response.AgreementResponseDTO;
import com.itjima_server.security.CustomUserDetails;
import com.itjima_server.service.AgreementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agreements")
@RequiredArgsConstructor
public class AgreementController {

    private final AgreementService agreementService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody AgreementCreateRequestDTO req,
            @AuthenticationPrincipal CustomUserDetails user) {
        AgreementResponseDTO res = agreementService.create(user.getId(), req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(HttpStatus.CREATED.value(), "대여 요청 성공", res));
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<?> accept(@PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user) {
        AgreementResponseDTO res = agreementService.accept(user.getId(), id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDTO.success(HttpStatus.OK.value(), "대여 승인 성공", res));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user) {
        AgreementResponseDTO res = agreementService.reject(user.getId(), id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDTO.success(HttpStatus.OK.value(), "대여 거절 성공", res));
    }

}
