package com.itjima_server.domain.item;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ItemStatus {
    AVAILABLE("대여가능"),
    PENDING_APPROVAL("승인 대기 중"),
    ON_LOAN("대여 중");

    private final String description;
}
