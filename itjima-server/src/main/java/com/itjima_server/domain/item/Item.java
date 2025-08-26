package com.itjima_server.domain.item;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Item {

    private long id;
    private long userId;
    private ItemType type;
    private String title;
    private String description;
    private ItemStatus status;
    private String fileUrl;
    private String fileType;
    private LocalDateTime createdAt;
}
