package com.itjima_server.dto.agreement.response;

import com.itjima_server.domain.AgreementStatus;
import com.itjima_server.domain.ItemStatus;
import com.itjima_server.domain.ItemType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgreementDetailDTO {

    // Agreemnet 정보
    private long agreementId;
    private AgreementStatus agreementStatus;
    private BigDecimal amount;
    private LocalDateTime dueAt;
    private String terms;
    private LocalDateTime createdAt;

    // Item 정보
    private long itemId;
    private ItemType itemType;
    private String itemTitle;
    private String itemDescription;
    private ItemStatus itemStatus;
    private String itemFileUrl;
    private String itemFileType;

    // 채권자(Creditor) 정보
    private long creditorId;
    private String creditorName;
    private LocalDateTime creditorConfirmAt;

    // 채무자(Debtor) 정보
    private long debtorId;
    private String debtorName;
    private LocalDateTime debtorConfirmAt;
}
