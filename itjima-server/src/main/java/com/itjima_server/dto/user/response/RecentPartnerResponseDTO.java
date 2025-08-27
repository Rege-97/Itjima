package com.itjima_server.dto.user.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "최근 거래 상대 응답 DTO")
public class RecentPartnerResponseDTO {

    @Schema(description = "상대방 사용자 ID", example = "101")
    private long id;

    @Schema(description = "마지막 계약 ID", example = "5501")
    private long lastAgreementId;

    @Schema(description = "상대방 이름", example = "홍길동")
    private String name;

    @Schema(description = "상대방 이메일", example = "partner@example.com")
    private String email;

    @Schema(description = "상대방 전화번호", example = "010-1234-5678")
    private String phone;

    @Schema(description = "최근 거래일시", example = "2025-08-27 14:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastContactAt;
}
