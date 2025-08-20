package com.itjima_server.util;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileResult {

    private final String fileUrl;
    private final String fileType;
}
