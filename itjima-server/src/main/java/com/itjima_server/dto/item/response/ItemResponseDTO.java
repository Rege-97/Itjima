package com.itjima_server.dto.item.response;

import com.itjima_server.domain.Item;
import com.itjima_server.domain.ItemType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ItemResponseDTO {

    private long id;
    private long userId;
    private ItemType type;
    private String title;
    private String description;
    private String fileUrl;
    private String fileType;
    private LocalDateTime createdAt;

    public static ItemResponseDTO from(Item item) {
        return new ItemResponseDTO(item.getId(), item.getUserId(), item.getType(), item.getTitle(),
                item.getDescription(), item.getFileUrl(), item.getFileType(), item.getCreatedAt());
    }
}
