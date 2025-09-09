package com.itjima_server.dto.dashboard.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "연체 대여 DTO")
public class DashboardOverdueDTO {

    @Schema(description = "대여 ID", example = "12")
    private Long id;

    @Schema(description = "대여 금액 (금전 대여 시)", example = "50000")
    private Long amount;

    @Schema(description = "반납일", example = "2025-09-15")
    private String dueAt;

    @Schema(description = "사용자 역할 (CREDITOR: 채권자, DEBTOR: 채무자)", example = "CREDITOR")
    private String role;

    @Schema(description = "대여물품 제목", example = "MacBook Pro 16인치")
    private String itemTitle;

    @Schema(description = "대여물품 설명", example = "애플 M1 프로세서 장착 노트북")
    private String itemDescription;

    @Schema(description = "대여물품 파일 URL", example = "https://cdn.example.com/files/abc.png")
    private String itemFileUrl;

    @Schema(description = "연체 일수", example = "5")
    private Integer overDays;
}
