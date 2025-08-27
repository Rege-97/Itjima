package com.itjima_server.dto.user.swagger;

import com.itjima_server.common.PagedResultDTO;
import com.itjima_server.dto.user.response.RecentPartnerResponseDTO;
import java.util.List;

public class RecentPartnerPagedResponse extends PagedResultDTO<RecentPartnerResponseDTO> {

    public RecentPartnerPagedResponse(List<RecentPartnerResponseDTO> items, boolean hasNext,
            Long lastId) {
        super(items, hasNext, lastId);
    }
}
