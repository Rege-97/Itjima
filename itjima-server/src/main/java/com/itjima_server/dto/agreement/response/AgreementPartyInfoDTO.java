package com.itjima_server.dto.agreement.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.itjima_server.domain.AgreementParty;
import com.itjima_server.domain.AgreementPartyRole;
import com.itjima_server.dto.user.response.UserSimpleInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "대여 요청 참여자 정보 DTO")
public class AgreementPartyInfoDTO {

    @Schema(description = "사용자 기본 정보")
    private UserSimpleInfoDTO user;

    @Schema(description = "대여 참여자의 역할", example = "CREDITOR")
    private AgreementPartyRole role;

    @Schema(description = "확정 시각", example = "2025-08-25 13:45:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime confirmAt;


    public static AgreementPartyInfoDTO from(AgreementParty agreementParty,
            UserSimpleInfoDTO user) {
        return new AgreementPartyInfoDTO(user, agreementParty.getRole(),
                agreementParty.getConfirmAt());
    }
}