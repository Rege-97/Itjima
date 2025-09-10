package com.itjima_server.dto.dashboard.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "활성화 대여 카운트 DTO")
public class DashboardAgreementCountResponseDTO {

    @Schema(description = "사용자 역할 (CREDITOR: 채권자, DEBTOR: 채무자)", example = "CREDITOR")
    private String role;

    @Schema(description = "해당 역할의 계약 건수", example = "5")
    private Long count;
}
