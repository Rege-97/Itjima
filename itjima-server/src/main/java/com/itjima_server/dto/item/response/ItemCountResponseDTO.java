package com.itjima_server.dto.item.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Schema(description = "대여 물품 개수 DTO")
public class ItemCountResponseDTO {

    @Schema(description = "총 대여물품 개수", example = "123")
    private int itemAllCount;

    @Schema(description = "대여중인 대여물품 개수", example = "123")
    private int itemLoanCount;

    @Schema(description = "대여 가능한 대여물품 개수", example = "123")
    private int itemAvailableCount;

}
