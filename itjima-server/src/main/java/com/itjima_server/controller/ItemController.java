package com.itjima_server.controller;

import com.itjima_server.common.ApiResponseDTO;
import com.itjima_server.common.PagedResultDTO;
import com.itjima_server.dto.item.request.ItemCreateRequestDTO;
import com.itjima_server.dto.item.request.ItemUpdateRequestDTO;
import com.itjima_server.dto.item.response.ItemResponseDTO;
import com.itjima_server.dto.item.swagger.ItemPagedResponse;
import com.itjima_server.dto.item.swagger.ItemSummaryPagedResponse;
import com.itjima_server.security.CustomUserDetails;
import com.itjima_server.service.ItemService;
import com.itjima_server.util.FileResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

/**
 * 대여물품 관련 API 클래스
 *
 * @author Rege-97
 * @since 2025-08-22
 */
@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
@Tag(name = "Item", description = "대여 물품 API")
@SecurityRequirement(name = "bearerAuth")
public class ItemController {

    private final ItemService itemService;

    /**
     * 대여물품 등록 처리
     *
     * @param req  대여물품 등록 요청 DTO
     * @param user 로그인한 사용자
     * @return 대여물품 등록 응답
     */
    @Operation(
            summary = "물품 등록",
            description = "대여 물품 신규 등록",
            responses = {
                    @ApiResponse(responseCode = "201", description = "물품 등록 성공",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ItemResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "요청 검증 실패",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                    @ApiResponse(responseCode = "401", description = "인증 필요",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            }
    )
    @PostMapping
    public ResponseEntity<?> create(@AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody ItemCreateRequestDTO req) {
        ItemResponseDTO res = itemService.create(req, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(HttpStatus.CREATED.value(), "물품 등록 성공", res));
    }

    /**
     * 대여물품 수정 처리
     *
     * @param req  대여물품 수정 요청 DTO
     * @param user 로그인한 사용자
     * @param id   수정할 물품 id
     * @return 수정 결과 응답
     */
    @Operation(
            summary = "물품 수정",
            description = "대여 물품 정보 수정",
            responses = {
                    @ApiResponse(responseCode = "200", description = "물품 수정 성공",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ItemResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "요청 검증 실패",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                    @ApiResponse(responseCode = "401", description = "인증 필요",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                    @ApiResponse(responseCode = "403", description = "권한 없음",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                    @ApiResponse(responseCode = "404", description = "대상 물품 없음",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody ItemUpdateRequestDTO req, @PathVariable Long id) {
        ItemResponseDTO res = itemService.update(req, user.getId(), id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDTO.success(HttpStatus.OK.value(), "물품 수정 성공", res));
    }

    /**
     * 이미지 저장 및 DB 경로 수정 처리
     *
     * @param id  이미지를 저장할 물품 id
     * @param img 저장할 이미지 파일
     * @return 저장된 경로 응답
     */
    @Operation(
            summary = "물품 이미지 업로드",
            description = "이미지 저장 및 경로 업데이트",
            responses = {
                    @ApiResponse(responseCode = "200", description = "이미지 저장 성공",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = FileResult.class))),
                    @ApiResponse(responseCode = "400", description = "요청 검증 실패",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                    @ApiResponse(responseCode = "401", description = "인증 필요",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                    @ApiResponse(responseCode = "403", description = "권한 없음",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                    @ApiResponse(responseCode = "404", description = "대상 물품 없음",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            }
    )
    @PostMapping("/{id}/file")
    public ResponseEntity<?> saveImage(@PathVariable Long id, @RequestPart MultipartFile img) {
        FileResult res = itemService.saveImage(id, img);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDTO.success(HttpStatus.OK.value(), "이미지 저장 성공", res));
    }

    /**
     * 대여 물품 리스트 조회
     *
     * @param user   로그인한 사용자
     * @param lastId 조회할 마지막 id
     * @param size   한 페이지에 보여줄 개수
     * @return 대여 물품 리스트 응답 DTO
     */
    @Operation(
            summary = "물품 목록 조회(커서 기반)",
            description = "lastId와 size로 커서 기반 페이지네이션",
            responses = {
                    @ApiResponse(responseCode = "200", description = "물품 목록 조회 성공",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ItemPagedResponse.class))),
                    @ApiResponse(responseCode = "401", description = "인증 필요",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            }
    )
    @GetMapping
    public ResponseEntity<?> getList(@RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails user) {
        PagedResultDTO<?> res = itemService.getList(user.getId(), lastId, size);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDTO.success(HttpStatus.OK.value(), "물품 목록 조회 성공", res));
    }

    /**
     * 대여 물품 상세 조회
     *
     * @param id   조회할 물품 id
     * @param user 로그인한 사용자
     * @return 조회된 물품 응답 DTO
     */
    @Operation(
            summary = "물품 상세 조회",
            description = "물품 식별자로 상세 조회",
            responses = {
                    @ApiResponse(responseCode = "200", description = "물품 조회 성공",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ItemResponseDTO.class))),
                    @ApiResponse(responseCode = "401", description = "인증 필요",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                    @ApiResponse(responseCode = "403", description = "권한 없음",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                    @ApiResponse(responseCode = "404", description = "대상 물품 없음",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user) {
        ItemResponseDTO res = itemService.get(id, user.getId());
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDTO.success(HttpStatus.OK.value(), "물품 조회 성공", res));
    }

    /**
     * 화면 렌더링용 대여 물품 리스트 조회
     *
     * @param user   로그인한 사용자
     * @param lastId 조회할 마지막 id
     * @param size   한 페이지에 보여줄 개수
     * @return 대여 물품 리스트 응답 DTO
     */
    @Operation(
            summary = "화면 렌더링용 물품 목록 조회(커서 기반)",
            description = "lastId와 size로 커서 기반 페이지네이션",
            responses = {
                    @ApiResponse(responseCode = "200", description = "물품 목록 조회 성공",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ItemSummaryPagedResponse.class))),
                    @ApiResponse(responseCode = "401", description = "인증 필요",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            }
    )
    @GetMapping("/summary")
    public ResponseEntity<?> getSummaries(@RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails user) {
        PagedResultDTO<?> res = itemService.getSummaries(user.getId(), lastId, size);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDTO.success(HttpStatus.OK.value(), "물품 목록 조회 성공", res));
    }
}
