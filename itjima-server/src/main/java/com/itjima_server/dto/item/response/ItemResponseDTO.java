package com.itjima_server.dto.item.response;

import com.itjima_server.domain.Item;
import com.itjima_server.domain.ItemType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "물품 응답 DTO")
public class ItemResponseDTO {

    @Schema(description = "물품 ID", example = "123")
    private long id;

    @Schema(description = "소유자(등록자) 사용자 ID", example = "10")
    private long userId;

    @Schema(description = "물품 유형", implementation = ItemType.class, example = "OBJECT")
    private ItemType type;

    @Schema(description = "제목", example = "맥북 충전기 67W")
    private String title;

    @Schema(description = "설명", example = "C타입 정품 충전기. 생활 스크래치 있음.")
    private String description;

    @Schema(description = "첨부 파일 URL(이미지 등)", example = "/uploads/items/10/abc123.jpg")
    private String fileUrl;

    @Schema(description = "첨부 파일 MIME 타입", example = "image/jpeg")
    private String fileType;

    @Schema(description = "등록일시", example = "2025-08-22 13:45:10")
    private LocalDateTime createdAt;


    public static ItemResponseDTO from(Item item) {
        return new ItemResponseDTO(item.getId(), item.getUserId(), item.getType(), item.getTitle(),
                item.getDescription(), item.getFileUrl(), item.getFileType(), item.getCreatedAt());
    }
}
