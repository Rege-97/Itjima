package com.itjima_server.dto.notification.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.itjima_server.domain.notification.Notification;
import com.itjima_server.domain.notification.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Schema(description = "알림 응답 DTO")
public class NotificationResponseDTO {

    @Schema(description = "알림 ID", example = "123")
    private long id;

    @Schema(description = "대여(Agreement) ID", example = "987")
    private long agreementId;

    @Schema(description = "수신자 사용자 ID", example = "1001")
    private long userId;

    @Schema(description = "알림 유형", implementation = NotificationType.class, example = "REMINDER")
    private NotificationType type;

    @Schema(description = "알림 메시지", example = "[반납일 알림] 반납일을 잊지 마세요!")
    private String message;

    @Schema(description = "생성 시각", example = "2025-08-27 03:21:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "읽은 시각(미읽음이면 null)", nullable = true, example = "2025-08-27 04:10:12")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readAt;

    public static NotificationResponseDTO from(Notification notification) {
        return NotificationResponseDTO.builder()
                .id(notification.getId())
                .agreementId(notification.getAgreementId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .message(notification.getMessage())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
