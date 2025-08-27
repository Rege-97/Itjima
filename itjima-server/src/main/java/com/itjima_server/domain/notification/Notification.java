package com.itjima_server.domain.notification;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Notification {

    private long id;
    private long agreementId;
    private long userId;
    private NotificationType type;
    private String message;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}
