package com.itjima_server.controller;

import com.itjima_server.common.ApiResponseDTO;
import com.itjima_server.common.PagedResultDTO;
import com.itjima_server.domain.agreement.AgreementPartyRole;
import com.itjima_server.dto.agreement.request.AgreementCreateRequestDTO;
import com.itjima_server.dto.agreement.request.AgreementExtendRequestDTO;
import com.itjima_server.dto.agreement.request.AgreementUpdateRequestDTO;
import com.itjima_server.dto.agreement.response.AgreementDetailResponseDTO;
import com.itjima_server.dto.agreement.response.AgreementResponseDTO;
import com.itjima_server.dto.agreement.swagger.AgreementPagedResponse;
import com.itjima_server.dto.transaction.request.TransactionCreateRequestDTO;
import com.itjima_server.dto.transaction.response.TransactionResponseDTO;
import com.itjima_server.dto.transaction.swagger.TransactionPagedResponse;
import com.itjima_server.security.CustomUserDetails;
import com.itjima_server.service.AgreementService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 대여 약정 관련 API 클래스
 *
 * @author Rege-97
 * @since 2025-08-26
 */
@RestController
@RequestMapping("/api/agreements")
@RequiredArgsConstructor
@Tag(name = "Agreement", description = "대여 약정 API")
@SecurityRequirement(name = "bearerAuth")
public class AgreementController {

    private final AgreementService agreementService;

    /**
     * 대여 요청 등록 처리
     *
     * @param req  대여 요청 등록 DTO
     * @param user 로그인한 사용자
     * @return 등록된 대여 요청 응답
     */
    @Operation(
            summary = "대여 요청 등록",
            description = "대여 요청 신규 생성",
            responses = {
                    @ApiResponse(responseCode = "201", description = "대여 요청 등록 성공",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = AgreementResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "요청 검증 실패",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                    @ApiResponse(responseCode = "401", description = "인증 필요",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                    @ApiResponse(responseCode = "403", description = "권한 없음",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                    @ApiResponse(responseCode = "404", description = "대상 사용자/물품 없음",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                    @ApiResponse(responseCode = "409", description = "대여 불가 상태",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            }
    )
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody AgreementCreateRequestDTO req,
            @AuthenticationPrincipal CustomUserDetails user) {
        AgreementResponseDTO res = agreementService.create(user.getId(), req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(HttpStatus.CREATED.value(), "대여 요청 성공", res));
    }

    /**
     * 대여 승인 처리 (채무자만 가능)
     *
     * @param id   대여 ID
     * @param user 로그인한 유저
     * @return 승인 처리된 대여 응답
     */
    @Operation(
            summary = "대여 승인",
            description = "채무자의 대여 승인 처리",
            responses = {
                    @ApiResponse(responseCode = "200", description = "대여 승인 성공",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = AgreementResponseDTO.class))),
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
    @PutMapping("/{id}/accept")
    public ResponseEntity<?> accept(@PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user) {
        AgreementResponseDTO res = agreementService.accept(user.getId(), id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDTO.success(HttpStatus.OK.value(), "대여 승인 성공", res));
    }

    /**
     * 대여 거절 처리 (채무자만 가능)
     *
     * @param id   대여 ID
     * @param user 로그인한 유저
     * @return 거절 처리된 대여 응답
     */
    @Operation(
            summary = "대여 거절",
            description = "채무자의 대여 거절 처리",
            responses = {
                    @ApiResponse(responseCode = "200", description = "대여 거절 성공",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = AgreementResponseDTO.class))),
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
        AgreementResponseDTO res = agreementService.reject(user.getId(), id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDTO.success(HttpStatus.OK.value(), "대여 거절 성공", res));
    }

    /**
     * 대여 취소 처리 (채권자만 가능, PENDING 상태에서 가능)
     *
     * @param id   대여 ID
     * @param user 로그인한 유저
     * @return 취소 처리된 대여 응답
     */
    @Operation(
            summary = "대여 취소",
            description = "채권자의 대여 요청 중 대여 취소 처리",
            responses = {
                    @ApiResponse(responseCode = "200", description = "대여 취소 성공",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = AgreementResponseDTO.class))),
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
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user) {
        AgreementResponseDTO res = agreementService.cancel(user.getId(), id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDTO.success(HttpStatus.OK.value(), "대여 취소 성공", res));
    }

    /**
     * 대여 완료 처리 (채권자만 가능, ACCEPTED/OVERDUE → COMPLETED)
     *
     * @param id   대여 ID
     * @param user 로그인한 유저
     * @return 완료 처리된 대여 응답
     */
    @Operation(
            summary = "대여 완료",
            description = "채권자의 대여 완료 처리",
            responses = {
                    @ApiResponse(responseCode = "200", description = "대여 완료 성공",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = AgreementResponseDTO.class))),
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
    @PutMapping("/{id}/complete")
    public ResponseEntity<?> complete(@PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user) {
        AgreementResponseDTO res = agreementService.complete(user.getId(), id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDTO.success(HttpStatus.OK.value(), "대여 완료 성공", res));
    }

    /**
     * 대여 목록 조회 (무한 스크롤 커서 기반)
     *
     * @param lastId 조회할 마지막 id
     * @param role   조회 관점(CREDITOR/DEBTOR)
     * @param size   한 페이지에 보여줄 개수
     * @param user   로그인한 유저
     * @return 항목(items), hasNext, lastId를 포함한 페이지 응답
     */
    @Operation(
            summary = "대여 목록 조회(커서 기반)",
            description = "lastId와 size로 커서 기반 페이지네이션",
            responses = {
                    @ApiResponse(responseCode = "200", description = "대여 목록 조회 성공",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = AgreementPagedResponse.class))),
                    @ApiResponse(responseCode = "401", description = "인증 필요",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            }
    )
    @GetMapping
    public ResponseEntity<?> getList(@RequestParam(required = false) Long lastId,
            @RequestParam AgreementPartyRole role,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails user) {
        PagedResultDTO<?> res = agreementService.getList(user.getId(), lastId, size, role);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDTO.success(HttpStatus.OK.value(), "대여 목록 조회 성공", res));
    }

    /**
     * 대여 단건 상세 조회
     *
     * @param id   대여 ID
     * @param user 로그인한 유저
     * @return 대여 상세 응답
     */
    @Operation(
            summary = "대여 상세 조회",
            description = "대여 단건 상세 조회 처리",
            responses = {
                    @ApiResponse(responseCode = "200", description = "대여 상세 조회 성공",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = AgreementDetailResponseDTO.class))),
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
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user) {
        AgreementDetailResponseDTO res = agreementService.get(user.getId(), id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDTO.success(HttpStatus.OK.value(), "대여 상세 조회 성공", res));
    }

    /**
     * 금전 상환 요청 (채무자만 가능)
     *
     * @param id   대여 ID
     * @param req  상환 요청 DTO
     * @param user 로그인한 유저
     * @return 상환 요청 완료 응답
     */
    @Operation(
            summary = "금전 상환 요청",
            description = "채무자가 채권자에게 금전 상환 요청 처리",
            responses = {
                    @ApiResponse(responseCode = "200", description = "대여 상세 조회 성공",
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
    @PostMapping("/{id}/transactions")
    public ResponseEntity<?> createTransaction(@PathVariable Long id,
            @Valid @RequestBody TransactionCreateRequestDTO req,
            @AuthenticationPrincipal CustomUserDetails user) {
        TransactionResponseDTO res = agreementService.createTransaction(user.getId(), id,
                req.getAmount());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(HttpStatus.CREATED.value(), "상환 요청 성공", res));
    }

    /**
     * 대여 목록 조회 (무한 스크롤 커서 기반)
     *
     * @param id     대여 ID
     * @param user   로그인한 사용자
     * @param lastId 마지막으로 조회한 대여 ID
     * @param size   요청한 페이지 크기
     * @return 항목(items), hasNext, lastId를 포함한 페이지 응답
     */
    @Operation(
            summary = "상환 내역 조회",
            description = "해당 대여의 상환 내역 조회",
            responses = {
                    @ApiResponse(responseCode = "200", description = "상환 내역 조회 성공",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = TransactionPagedResponse.class))),
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
    @GetMapping("/{id}/transactions")
    public ResponseEntity<?> getTransactionList(@PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "20") int size) {
        PagedResultDTO<?> res = agreementService.getTransactionList(user.getId(), id, lastId, size);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDTO.success(HttpStatus.OK.value(), "대여 목록 조회 성공", res));
    }

    /**
     * 대여 기간 연장
     *
     * @param id   대여 ID
     * @param user 로그인한 사용자
     * @param req  연장할 기간 요청
     * @return 변경한 대여 정보
     */
    @Operation(
            summary = "대여 기간 연장",
            description = "채권자의 대여기간 연장",
            responses = {
                    @ApiResponse(responseCode = "200", description = "대여 기간 연장 성공",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = AgreementResponseDTO.class))),
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
    @PutMapping("/{id}/extend")
    public ResponseEntity<?> extendAgreement(@PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody AgreementExtendRequestDTO req) {
        AgreementResponseDTO res = agreementService.agreementExtend(id, user.getId(),
                req.getDueAt());
        return ResponseEntity.ok(
                ApiResponseDTO.success(HttpStatus.OK.value(), "대여 기간 연장 성공", res));
    }

    /**
     * 대여 내용 수정
     *
     * @param id   대여 ID
     * @param user 로그인한 사용자
     * @param req  수정할 대여 내용 요청
     * @return 수정된 대여 응답
     */
    @Operation(
            summary = "대여 내용 수정",
            description = "채권자의 대여 내용 수정 요청 처리",
            responses = {
                    @ApiResponse(responseCode = "200", description = "대여 내용 수정 요청 성공",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = AgreementResponseDTO.class))),
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
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateAgreement(@PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody AgreementUpdateRequestDTO req) {
        AgreementResponseDTO res = agreementService.updateAgreementTerms(id, user.getId(),
                req.getTerms());
        return ResponseEntity.ok(
                ApiResponseDTO.success(HttpStatus.OK.value(), "대여 내용 수정 요청 성공", res));
    }

}
