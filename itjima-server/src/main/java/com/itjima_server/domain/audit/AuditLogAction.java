package com.itjima_server.domain.audit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuditLogAction {

    AGREEMENT_CREATE("대여 요청 생성"),
    AGREEMENT_ACCEPT("대여 수락"),
    AGREEMENT_REJECT("대여 거절"),
    AGREEMENT_CANCEL("대여 취소"),
    AGREEMENT_COMPLETE("대여 완료"),
    TRANSACTION_CREATE("상환 요청 생성"),
    TRANSACTION_CONFIRM("상환 승인"),
    TRANSACTION_REJECT("상환 거절");

    private final String description;
}
