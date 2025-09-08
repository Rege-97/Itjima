package com.itjima_server.dto.agreement.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.itjima_server.domain.audit.AuditLog;
import com.itjima_server.domain.audit.AuditLogAction;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Schema(description = "대여 활동 로그 DTO")
public class AgreementLogsResponseDTO {

    @Schema(description = "로그 ID", example = "101")
    private Long id;

    @Schema(description = "로그 남긴 사용자 이름", example = "홍길동")
    private String userName;

    @Schema(description = "로그 액션 타입", example = "ACCEPTED")
    private AuditLogAction action;

    @Schema(description = "세부 내용", example = "대여 승낙 완료")
    private String detail;

    @Schema(description = "로그 생성일", example = "2025-09-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
