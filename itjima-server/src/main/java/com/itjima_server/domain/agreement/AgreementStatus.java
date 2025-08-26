package com.itjima_server.domain.agreement;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AgreementStatus {
    PENDING("승인 대기 중"),
    ACCEPTED("수락됨 (대여 중)"),
    REJECTED("거절됨"),
    COMPLETED("완료됨"),
    CANCELED("취소됨"),
    OVERDUE("연체됨");

    private final String description;
}
