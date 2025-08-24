package com.itjima_server.domain;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgreementParty {

    private long id;
    private long agreementId;
    private long userId;
    private AgreementPartyRole role;
    private LocalDateTime confirmAt;
}
