package com.itjima_server.domain.transaction;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransactionType {

    REPAYMENT("상환");

    private final String description;
}
