package com.itjima_server.dto.dashboard.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "요청 알림 응답 DTO")
public class DashboardPendingResponseDTO {

    @Schema(description = "알림 출처 (AGREEMENT 또는 TRANSACTION)")
    private String source;

    @Schema(description = "원본 ID (AGREEMENT.id 또는 TRANSACTION.id)")
    private Long id;

    @Schema(description = "알림 상태 (예: 대여 승인 요청, 상환 완료 요청)")
    private String status;

    @Schema(description = "요청을 발생시킨 사용자 이름")
    private String pendingUser;

    @Schema(description = "알림 설명 (물품 제목 또는 금액+원)")
    private String description;

    @Schema(description = "커서 키 (created_at → timestamp ms 단위)")
    private Long cursorKey;
}
