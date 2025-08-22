package com.itjima_server.dto.agreement.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.itjima_server.domain.AgreementParty;
import com.itjima_server.domain.AgreementPartyRole;
import com.itjima_server.dto.user.response.UserSimpleInfoDTO;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AgreementPartyInfoDTO {

    private UserSimpleInfoDTO user;
    private AgreementPartyRole role;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime confirmAt;

    public static AgreementPartyInfoDTO from(AgreementParty agreementParty,
            UserSimpleInfoDTO user) {
        return new AgreementPartyInfoDTO(user, agreementParty.getRole(),
                agreementParty.getConfirmAt());
    }
}