package com.itjima_server.dto.agreement.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "대여 연장 요청 DTO")
public class AgreementExtendRequestDTO {

    @NotNull(message = "dueAt은 필수입니다.")
    @Future(message = "dueAt은 미래 시간이여야 합니다.")
    private LocalDateTime dueAt;
}
