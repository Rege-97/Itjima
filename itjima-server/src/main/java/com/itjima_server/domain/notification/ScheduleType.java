package com.itjima_server.domain.notification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ScheduleType {

    D_MINUS_7("D-7"),
    D_MINUS_3("D-3"),
    D_MINUS_1("D-1"),
    D_DAY("D-DAY"),
    OVERDUE("연체");

    private final String description;
}