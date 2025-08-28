package com.itjima_server.dto.agreement.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "대여 변경 요청 DTO")
public class AgreementUpdateRequestDTO {

    @NotBlank(message = "terms는 비어 있을 수 없습니다.")
    @Size(max = 1000, message = "terms는 1000자를 초과할 수 없습니다.")
    private String terms;
}
