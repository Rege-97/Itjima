package com.itjima_server.dto.agreement.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "대여 리스트 응답 DTO")
public class AgreementSummaryResponseDTO {
    @Schema(description = "대여 ID", example = "123")
    private Long id;

    @Schema(description = "대여 상태", example = "IN_PROGRESS")
    private String status;

    @Schema(description = "원금/대여금(금전 항목일 때 기준 금액)", example = "150000.00")
    private BigDecimal amount;

    @Schema(description = "조건", example = "매 주 수요일 상환")
    private String terms;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "만기일", example = "2025-09-30 14:12:30")
    private LocalDateTime dueAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "반납/완료 처리일(물품이면 반납일, 금전이면 완납 처리일 등)", example = "2025-09-30 14:12:30")
    private LocalDateTime returnDate;

    @Schema(description = "대여물품 제목", example = "생활비 대여")
    private String itemTitle;

    @Schema(description = "대여물품 타입", example = "MONEY")
    private String itemType;

    @Schema(description = "대여물품 파일 URL(있을 경우)", example = "https://cdn.example.com/files/abc.png")
    private String itemFileUrl;

    @Schema(description = "상대방 이름(파트너)", example = "김철수")
    private String partnerName;

    @Schema(description = "잔액(아이템 타입이 MONEY일 때만 계산됨, 그 외는 null)", example = "90000.00", nullable = true)
    private BigDecimal remainingAmount;
}
