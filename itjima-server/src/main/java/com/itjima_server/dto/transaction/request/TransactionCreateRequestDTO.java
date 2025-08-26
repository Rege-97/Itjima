package com.itjima_server.dto.transaction.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "거래 생성 요청 DTO")
public class TransactionCreateRequestDTO {

    @NotNull(message = "상환 금액은 필수 값입니다.")
    @DecimalMin(value = "100.00", inclusive = true, message = "상환 금액은 100.00 이상이어야 합니다.")
    @Schema(description = "상환 금액", example = "10000.00")
    private BigDecimal amount;
}
