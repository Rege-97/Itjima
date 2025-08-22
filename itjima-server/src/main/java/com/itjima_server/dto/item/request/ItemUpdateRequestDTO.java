package com.itjima_server.dto.item.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "물품 수정 요청 DTO")
public class ItemUpdateRequestDTO {

    @Schema(description = "제목(최대 100자)", example = "맥북 충전기 67W - 상태 양호")
    @NotBlank(message = "제목은 비어 있을 수 없습니다.")
    @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.")
    private String title;

    @Schema(description = "설명(최대 500자)", example = "생활 스크래치 약간, 정상 작동")
    @NotNull(message = "설명은 필수 항목입니다.")
    @Size(max = 500, message = "설명은 500자를 초과할 수 없습니다.")
    private String description;
}