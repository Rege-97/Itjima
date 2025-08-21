package com.itjima_server.controller;

import com.itjima_server.common.ApiResponseDTO;
import com.itjima_server.common.PagedResultDTO;
import com.itjima_server.dto.item.request.ItemCreateRequestDTO;
import com.itjima_server.dto.item.request.ItemUpdateRequestDTO;
import com.itjima_server.dto.item.response.ItemResponseDTO;
import com.itjima_server.security.CustomUserDetails;
import com.itjima_server.service.ItemService;
import com.itjima_server.util.FileResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<?> create(@AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody ItemCreateRequestDTO req) {
        ItemResponseDTO res = itemService.create(req, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(HttpStatus.CREATED.value(), "물품 등록 성공", res));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody ItemUpdateRequestDTO req, @PathVariable Long id) {
        ItemResponseDTO res = itemService.update(req, user.getId(), id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDTO.success(HttpStatus.OK.value(), "물품 수정 성공", res));
    }

    @PostMapping("/{id}/file")
    public ResponseEntity<?> saveImage(@PathVariable Long id, @RequestPart MultipartFile img) {
        FileResult res = itemService.saveImage(id, img);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDTO.success(HttpStatus.OK.value(), "이미지 저장 성공", res));
    }

    @GetMapping
    public ResponseEntity<?> getList(@RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails user) {
        PagedResultDTO<?> res = itemService.getList(user.getId(), lastId, size);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDTO.success(HttpStatus.OK.value(), "물품 목록 조회 성공", res));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user) {
        ItemResponseDTO res = itemService.get(id, user.getId());
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDTO.success(HttpStatus.OK.value(), "물품 조회 성공", res));
    }


}
