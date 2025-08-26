package com.itjima_server.dto.transaction.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.itjima_server.domain.transaction.Transaction;
import com.itjima_server.domain.transaction.TransactionStatus;
import com.itjima_server.domain.transaction.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Schema(description = "상환 관련 응답 DTO")
public class TransactionResponseDTO {

    @Schema(description = "거래 ID", example = "1")
    private long id;

    @Schema(description = "대여 계약 ID", example = "10")
    private long agreementId;

    @Schema(description = "거래 유형 (LOAN_OUT: 대여, REPAYMENT: 상환, DEPOSIT: 입금, REFUND: 환불)", example = "REPAYMENT")
    private TransactionType type;

    @Schema(description = "거래 금액", example = "50000.00")
    private BigDecimal amount;

    @Schema(description = "거래 상태 (PENDING: 대기, CONFIRMED: 확정, CANCELED: 취소)", example = "CONFIRMED")
    private TransactionStatus status;

    @Schema(description = "거래 생성일시", example = "2025-08-25 14:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public static TransactionResponseDTO from(Transaction transaction) {
        return TransactionResponseDTO.builder()
                .id(transaction.getId())
                .agreementId(transaction.getAgreementId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .status(transaction.getStatus())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
