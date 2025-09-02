package com.itjima_server.dto.item.swagger;

import com.itjima_server.common.PagedResultDTO;
import com.itjima_server.dto.item.response.ItemSummaryResponseDTO;
import java.util.List;

public class ItemSummaryPagedResponse extends PagedResultDTO<ItemSummaryResponseDTO> {

    public ItemSummaryPagedResponse(List<ItemSummaryResponseDTO> items, boolean hasNext,
            Long lastId) {
        super(items, hasNext, lastId);
    }
}
