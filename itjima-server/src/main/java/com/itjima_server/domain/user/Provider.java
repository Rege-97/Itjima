package com.itjima_server.domain.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Provider {
    LOCAL("로컬"),
    KAKAO("카카오");
    
    private final String description;
}
