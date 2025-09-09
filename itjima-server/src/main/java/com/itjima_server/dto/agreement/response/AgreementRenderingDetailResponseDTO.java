package com.itjima_server.dto.agreement.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.itjima_server.domain.agreement.AgreementPartyRole;
import com.itjima_server.domain.agreement.AgreementStatus;
import com.itjima_server.domain.item.ItemType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "대여 상세 DTO")
public class AgreementRenderingDetailResponseDTO {

    @Schema(description = "대여 ID", example = "1")
    private Long id;

    @Schema(description = "대여 상태", example = "COMPLETED")
    private AgreementStatus status;

    @Schema(description = "대여 금액 (금전 대여일 경우)", example = "100000")
    private Long amount;

    @Schema(description = "대여 조건 / 약관", example = "2개월 이내 반납")
    private String terms;

    @Schema(description = "반납 예정일", example = "2025-09-30 23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    @Schema(description = "실제 반납일", example = "2025-10-05 15:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime returnDate;

    @Schema(description = "대여 생성일", example = "2025-09-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "대여물품 ID", example = "10")
    private Long itemId;

    @Schema(description = "대여물품 제목", example = "MacBook Pro 16인치")
    private String itemTitle;

    @Schema(description = "대여물품 설명", example = "로지텍 MX Keys")
    private String itemDescription;

    @Schema(description = "대여물품 유형 (MONEY: 금전 / OBJECT: 물품)", example = "ITEM")
    private ItemType itemType;

    @Schema(description = "대여물품 파일 URL(있을 경우)", example = "https://cdn.example.com/files/abc.png")
    private String itemFileUrl;

    @Schema(description = "채권자 ID", example = "3")
    private Long creditorId;

    @Schema(description = "채권자 이름", example = "홍길동")
    private String creditorName;

    @Schema(description = "채권자 전화번호", example = "01012345678")
    private String creditorPhone;

    @Schema(description = "채권자 확정일", example = "2025-09-01 12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime creditorConfirmAt;

    @Schema(description = "채무자 ID", example = "4")
    private Long debtorId;

    @Schema(description = "채무자 이름", example = "김철수")
    private String debtorName;

    @Schema(description = "채무자 전화번호", example = "01012345678")
    private String debtorPhone;

    @Schema(description = "채무자 확정일", example = "2025-09-01 12:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime debtorConfirmAt;

    @Schema(description = "총 대여일", example = "7")
    private int rentalDays;

    @Schema(description = "연체 반납 여부", example = "true")
    private Boolean isOverdueReturn;

    @Schema(description = "잔액(아이템 타입이 MONEY일 때만 계산됨, 그 외는 null)", example = "90000.00", nullable = true)
    private BigDecimal remainingAmount;

    @Schema(description = "내 역할", example = "DEBTOR")
    private AgreementPartyRole myRole;
}
