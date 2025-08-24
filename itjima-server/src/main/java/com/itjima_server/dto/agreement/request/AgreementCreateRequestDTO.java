package com.itjima_server.dto.agreement.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "대여 생성 요청 DTO")
public class AgreementCreateRequestDTO {

    @Schema(description = "대여할 물품 ID", example = "101")
    @Positive(message = "itemId는 양수여야 합니다.")
    private long itemId;

    @DecimalMin(value = "0.01", message = "amount는 0보다 커야 합니다.")
    @Digits(integer = 12, fraction = 2, message = "amount는 소수점 2자리까지 허용됩니다.")
    private BigDecimal amount;

    @NotNull(message = "dueAt은 필수입니다.")
    @Future(message = "dueAt은 미래 시간이여야 합니다.")
    private LocalDateTime dueAt;

    @NotBlank(message = "terms는 비어 있을 수 없습니다.")
    @Size(max = 1000, message = "terms는 1000자를 초과할 수 없습니다.")
    private String terms;

    @Positive(message = "debtorUserId는 양수여야 합니다.")
    private long debtorUserId;
}
