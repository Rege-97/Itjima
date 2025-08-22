package com.itjima_server.dto.agreement.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.itjima_server.domain.Agreement;
import com.itjima_server.domain.AgreementStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AgreementResponseDTO {

    private long id;
    private long item_id;
    private BigDecimal amount;
    private AgreementStatus status;
    private LocalDateTime dueAt;
    private String terms;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private AgreementPartyInfoDTO creditor;
    private AgreementPartyInfoDTO debtor;

    public static AgreementResponseDTO from(Agreement agreement, AgreementPartyInfoDTO creditor,
            AgreementPartyInfoDTO debtor) {
        return new AgreementResponseDTO(agreement.getId(), agreement.getItemId(),
                agreement.getAmount(), agreement.getStatus(), agreement.getDueAt(),
                agreement.getTerms(),
                agreement.getCreatedAt(), creditor, debtor);
    }
}
