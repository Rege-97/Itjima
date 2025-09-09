package com.itjima_server.dto.dashboard.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "대시보드 전체 응답 DTO")
public class DashboardResponseDTO {

    @Schema(
            description = "대여 건수 목록",
            implementation = DashboardAgreementCountResponseDTO.class
    )
    private List<DashboardAgreementCountResponseDTO> counts;

    @Schema(
            description = "반납 예정 계약 목록 (7일 이내)",
            implementation = DashboardComingReturnDTO.class
    )
    private List<DashboardComingReturnDTO> comingReturns;

    @Schema(
            description = "연체 계약 목록",
            implementation = DashboardOverdueDTO.class
    )
    private List<DashboardOverdueDTO> overdues;
}
