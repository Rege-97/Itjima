package com.itjima_server.dto.agreement.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.itjima_server.domain.Agreement;
import com.itjima_server.domain.AgreementStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "대여 요청 응답 DTO")
public class AgreementResponseDTO {

    @Schema(description = "대여 요청 ID", example = "123")
    private Long id;

    @Schema(description = "물품 ID", example = "987")
    private Long itemId;

    @Schema(description = "대여 금액", example = "150000.00")
    private BigDecimal amount;

    @Schema(description = "대여 요청 상태", example = "PENDING")
    private AgreementStatus status;

    @Schema(description = "상환/반납 예정일", type = "string", example = "2025-09-10 00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dueAt;

    @Schema(description = "약정 조건(특약 등)", example = "연체 시 일 1% 지연 손해금")
    private String terms;

    @Schema(description = "요청 생성 일시", type = "string", example = "2025-08-25 00:12:34")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "채권자 정보(빌려주는 사람)")
    private AgreementPartyInfoDTO creditor;

    @Schema(description = "채무자 정보(빌리는 사람)")
    private AgreementPartyInfoDTO debtor;

    public static AgreementResponseDTO from(Agreement agreement, AgreementPartyInfoDTO creditor,
            AgreementPartyInfoDTO debtor) {
        return new AgreementResponseDTO(agreement.getId(), agreement.getItemId(),
                agreement.getAmount(), agreement.getStatus(), agreement.getDueAt(),
                agreement.getTerms(),
                agreement.getCreatedAt(), creditor, debtor);
    }
}
