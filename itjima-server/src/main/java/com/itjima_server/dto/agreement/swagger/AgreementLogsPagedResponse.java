package com.itjima_server.dto.agreement.swagger;

import com.itjima_server.common.PagedResultDTO;
import com.itjima_server.dto.agreement.response.AgreementLogsResponseDTO;
import java.util.List;

public class AgreementLogsPagedResponse extends PagedResultDTO<AgreementLogsResponseDTO> {

    public AgreementLogsPagedResponse(List<AgreementLogsResponseDTO> items, boolean hasNext,
            Long lastId) {
        super(items, hasNext, lastId);
    }
}
