package com.itjima_server.dto.item.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "대여물품 상세 정보 응답 DTO")
public class ItemDetailResponseDTO {

    @Schema(description = "대여물품 ID", example = "1")
    private Long id;

    @Schema(description = "대여물품 제목", example = "무선 키보드")
    private String title;

    @Schema(description = "대여물품 설명", example = "로지텍 MX Keys")
    private String description;

    @Schema(description = "대여물품 상태", example = "AVAILABLE")
    private String status;

    @Schema(description = "대여물품 이미지 URL", example = "https://example.com/item/1.png")
    private String fileUrl;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "대여물품 등록일", example = "2023-01-01 10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "현재 대여자 이름", example = "홍길동", nullable = true)
    private String currentDebtorName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "현재 대여 시작일", example = "2023-01-15 10:00:00", nullable = true)
    private LocalDateTime currentStartAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "현재 대여 반납 예정일", example = "2023-01-15 10:00:00", nullable = true)
    private LocalDateTime currentDueAt;

    @Schema(description = "총 대여 횟수", example = "5")
    private Integer rentalCount;

    @Schema(description = "총 대여일", example = "20")
    private Integer totalRentalDays;

    @Schema(description = "평균 대여일", example = "4")
    private Integer avgRentalDays;
}
