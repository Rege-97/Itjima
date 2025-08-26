package com.itjima_server.domain.transaction;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransactionStatus {
    PENDING("승인 대기 중"),
    CONFIRMED("승인됨"),
    REJECTED("거절됨");

    private final String description;
}