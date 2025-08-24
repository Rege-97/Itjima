package com.itjima_server.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AgreementPartyRole {
    CREDITOR("채권자"),
    DEBTOR("채무자");

    private final String description;
}