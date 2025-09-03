package com.itjima_server.dto.item.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "물품 대여 내역 DTO")
public class ItemAgreementHistoryResponseDTO {

    @Schema(description = "대여 ID", example = "1")
    private long id;

    @Schema(description = "대여자 이름", example = "홍길동", nullable = true)
    private String debtorName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "대여 시작일", example = "2023-01-15 10:00:00", nullable = true)
    private LocalDateTime startDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "대여 반납 예정일", example = "2023-01-15 10:00:00", nullable = true)
    private LocalDateTime dueDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "실제 반납일", example = "2023-01-15 10:00:00", nullable = true)
    private LocalDateTime returnDate;

    @Schema(description = "대여 상태", example = "COMPLETED")
    private String status;

    @Schema(description = "대여 내용", example = "물품 파손 시 원가 변상")
    private String terms;

    @Schema(description = "총 대여일", example = "7")
    private int rentalDays;

    @Schema(description = "연체 반납 여부", example = "true")
    private boolean overdueReturn;
}
