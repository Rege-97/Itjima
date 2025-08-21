package com.itjima_server.dto.item.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemUpdateRequestDTO {

    @NotBlank(message = "제목은 비어 있을 수 없습니다.")
    @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.")
    private String title;

    @NotNull(message = "설명은 필수 항목입니다.")
    @Size(max = 500, message = "설명은 500자를 초과할 수 없습니다.")
    private String description;

}
