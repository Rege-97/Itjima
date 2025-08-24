package com.itjima_server.dto.agreement.swagger;

import com.itjima_server.common.PagedResultDTO;
import com.itjima_server.dto.agreement.response.AgreementDetailResponseDTO;
import java.util.List;

public class AgreementPagedResponse extends PagedResultDTO<AgreementDetailResponseDTO> {

    public AgreementPagedResponse(List<AgreementDetailResponseDTO> items, boolean hasNext, Long lastId) {
        super(items, hasNext, lastId);
    }
}
