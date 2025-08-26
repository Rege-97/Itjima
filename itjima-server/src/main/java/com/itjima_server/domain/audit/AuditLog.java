package com.itjima_server.domain.audit;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuditLog {

    private Long id;
    private Long agreementId;
    private Long userId;
    private AuditLogAction action;
    private String detail;
    private LocalDateTime createdAt;
}
