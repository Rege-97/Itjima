package com.itjima_server.dto.transaction.response;

import com.itjima_server.domain.transaction.Transaction;
import com.itjima_server.domain.transaction.TransactionStatus;
import com.itjima_server.domain.transaction.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TransactionResponseDTO {

    private long id;
    private long agreementId;
    private TransactionType type;
    private BigDecimal amount;
    private TransactionStatus status;
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
