package com.itjima_server.dto.agreement.swagger;

import com.itjima_server.common.PagedResultDTO;
import com.itjima_server.dto.agreement.response.AgreementSummaryResponseDTO;
import java.util.List;

public class AgreementSummaryPagedResponse extends PagedResultDTO<AgreementSummaryResponseDTO> {

    public AgreementSummaryPagedResponse(List<AgreementSummaryResponseDTO> items, boolean hasNext,
            Long lastId) {
        super(items, hasNext, lastId);
    }
}
