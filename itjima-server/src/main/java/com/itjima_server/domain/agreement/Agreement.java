package com.itjima_server.domain.agreement;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Agreement {

    private long id;
    private long itemId;
    private AgreementStatus status;
    private BigDecimal amount;
    private LocalDateTime dueAt;
    private String terms;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
