package com.itjima_server.dto.agreement.response;

import com.itjima_server.domain.AgreementStatus;
import com.itjima_server.domain.ItemStatus;
import com.itjima_server.domain.ItemType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AgreementDetailResponseDTO {

    private long agreementId;
    private AgreementStatus agreementStatus;
    private BigDecimal amount;
    private LocalDateTime dueAt;
    private String terms;
    private LocalDateTime createdAt;

    private ItemInfo item;
    private PartyInfo creditor;
    private PartyInfo debtor;

    @Getter
    @Setter
    @Builder
    public static class ItemInfo {

        private Long itemId;
        private ItemType itemType;
        private String itemTitle;
        private String itemDescription;
        private ItemStatus itemStatus;
        private String itemFileUrl;
        private String itemFileType;
    }

    @Getter
    @Setter
    @Builder
    public static class PartyInfo {

        private long id;
        private String name;
        private LocalDateTime confirmAt;
    }

    public static AgreementDetailResponseDTO from(AgreementDetailDTO dto) {
        ItemInfo item = ItemInfo.builder()
                .itemId(dto.getItemId())
                .itemType(dto.getItemType())
                .itemTitle(dto.getItemTitle())
                .itemStatus(dto.getItemStatus())
                .itemDescription(dto.getItemDescription())
                .itemFileType(dto.getItemFileType())
                .itemFileUrl(dto.getItemFileUrl())
                .build();
        PartyInfo creditor = PartyInfo.builder()
                .id(dto.getCreditorId())
                .name(dto.getCreditorName())
                .confirmAt(dto.getCreditorConfirmAt())
                .build();
        PartyInfo debtor = PartyInfo.builder()
                .id(dto.getDebtorId())
                .name(dto.getDebtorName())
                .confirmAt(dto.getDebtorConfirmAt())
                .build();
        return AgreementDetailResponseDTO.builder()
                .agreementId(dto.getAgreementId())
                .agreementStatus(dto.getAgreementStatus())
                .amount(dto.getAmount())
                .dueAt(dto.getDueAt())
                .terms(dto.getTerms())
                .createdAt(dto.getCreatedAt())
                .item(item)
                .creditor(creditor)
                .debtor(debtor)
                .build();
    }
}
