package com.itjima_server.dto.dashboard.swagger;

import com.itjima_server.common.PagedResultDTO;
import com.itjima_server.dto.dashboard.response.DashboardPendingResponseDTO;
import java.util.List;

public class DashboardPendingPagedResponse extends PagedResultDTO<DashboardPendingResponseDTO> {

    public DashboardPendingPagedResponse(List<DashboardPendingResponseDTO> items, boolean hasNext,
            Long lastId) {
        super(items, hasNext, lastId);
    }
}
