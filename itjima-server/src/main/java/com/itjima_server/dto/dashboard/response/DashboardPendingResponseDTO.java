package com.itjima_server.dto.dashboard.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "요청 알림 응답 DTO")
public class DashboardPendingResponseDTO {

    @Schema(description = "알림 출처 (AGREEMENT 또는 TRANSACTION)", example = "AGREEMENT")
    private String source;

    @Schema(description = "원본 ID (AGREEMENT.id 또는 TRANSACTION.id)", example = "12345")
    private Long id;

    @Schema(description = "알림 상태 (예: 대여 승인 요청, 상환 완료 요청)", example = "대여 승인 요청")
    private String status;

    @Schema(description = "요청을 발생시킨 사용자 이름", example = "김철수")
    private String pendingUser;

    @Schema(description = "알림 설명 (물품 제목 또는 금액+원)", example = "아이패드 프로 11인치")
    private String description;

    @Schema(description = "커서 키 (created_at → timestamp ms 단위)", example = "1678886400000")
    private Long cursorKey;

    @Schema(description = "알림 생성 시간", example = "2025-09-08T15:16:01.849")
    private LocalDateTime createdAt;
}
