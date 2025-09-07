package com.itjima_server.dto.item.swagger;

import com.itjima_server.common.PagedResultDTO;
import com.itjima_server.dto.item.response.ItemAgreementHistoryResponseDTO;
import java.util.List;

public class ItemAgreementPagedResponse extends PagedResultDTO<ItemAgreementHistoryResponseDTO> {

    public ItemAgreementPagedResponse(List<ItemAgreementHistoryResponseDTO> items, boolean hasNext,
            Long lastId) {
        super(items, hasNext, lastId);
    }
}
