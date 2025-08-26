package com.itjima_server.dto.transaction.request;

import com.itjima_server.domain.transaction.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionCreateRequestDTO {

    @NotNull(message = "거래 금액은 필수 값입니다.")
    @DecimalMin(value = "100.00", inclusive = true, message = "상환 금액은 100.00 이상이어야 합니다.")
    private BigDecimal amount;
}
