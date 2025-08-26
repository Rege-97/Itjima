package com.itjima_server.domain.item;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ItemType {
    MONEY("금전"),
    OBJECT("물품");

    private final String description;
}
