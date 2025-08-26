package com.itjima_server.dto.agreement.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.itjima_server.domain.agreement.AgreementStatus;
import com.itjima_server.domain.item.ItemStatus;
import com.itjima_server.domain.item.ItemType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Schema(description = "대여 요청 상세 응답 DTO")
public class AgreementDetailResponseDTO {

    @Schema(description = "대여 요청 ID", example = "123")
    private Long agreementId;

    @Schema(description = "대여 요청 상태", example = "PENDING")
    private AgreementStatus agreementStatus;

    @Schema(description = "대여 금액", example = "150000.00")
    private BigDecimal amount;

    @Schema(description = "상환/반납 예정일", example = "2025-09-10 00:00:00")
    private LocalDateTime dueAt;

    @Schema(description = "약정 조건(특약 등)", example = "연체 시 일 1%의 지연 손해금 발생")
    private String terms;

    @Schema(description = "요청 생성 일시", example = "2025-08-25 00:12:34")
    private LocalDateTime createdAt;

    private ItemInfo item;
    private PartyInfo creditor;
    private PartyInfo debtor;

    @Getter
    @Setter
    @Builder
    @Schema(name = "ItemInfo", description = "대여 물품 요약 정보")
    public static class ItemInfo {

        @Schema(description = "물품 ID", example = "987")
        private Long itemId;

        @Schema(description = "물품 유형", example = "OBJECT")
        private ItemType itemType;

        @Schema(description = "물품 제목", example = "닌텐도 스위치 OLED")
        private String itemTitle;

        @Schema(description = "물품 설명", example = "박스/구성품 완비, 미세 사용감")
        private String itemDescription;

        @Schema(description = "물품 상태", example = "AVAILABLE")
        private ItemStatus itemStatus;

        @Schema(description = "대표 파일 URL", example = "https://cdn.example.com/items/987.jpg")
        private String itemFileUrl;

        @Schema(description = "파일 MIME 타입", example = "image/jpeg")
        private String itemFileType;
    }

    @Getter
    @Setter
    @Builder
    @Schema(name = "PartyInfo", description = "대여 참여자 요약 정보")
    public static class PartyInfo {

        @Schema(description = "사용자 ID", example = "42")
        private long id;

        @Schema(description = "사용자 이름", example = "홍길동")
        private String name;

        @Schema(description = "확정 시각", type = "string", example = "2025-08-25 13:45:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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
