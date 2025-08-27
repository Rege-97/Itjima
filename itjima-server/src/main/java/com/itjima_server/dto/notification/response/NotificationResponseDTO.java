package com.itjima_server.dto.notification.response;

import com.itjima_server.domain.notification.Notification;
import com.itjima_server.domain.notification.NotificationType;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class NotificationResponseDTO {

    private long id;
    private long agreementId;
    private long userId;
    private NotificationType type;
    private String message;
    private LocalDateTime createdAt;

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
