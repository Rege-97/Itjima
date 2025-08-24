package com.itjima_server.dto.item.swagger;

import com.itjima_server.common.PagedResultDTO;
import com.itjima_server.dto.item.response.ItemResponseDTO;
import java.util.List;

public class ItemPagedResponse extends PagedResultDTO<ItemResponseDTO> {

    public ItemPagedResponse(List<ItemResponseDTO> items, boolean hasNext, Long lastId) {
        super(items, hasNext, lastId);
    }
}
