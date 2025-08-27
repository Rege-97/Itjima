package com.itjima_server.domain.notification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

    REMINDER("반납일 임박"),
    OVERDUE("연체 발생");

    private final String description;
}