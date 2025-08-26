package com.itjima_server.dto.transaction.swagger;

import com.itjima_server.common.PagedResultDTO;
import com.itjima_server.dto.transaction.response.TransactionResponseDTO;
import java.util.List;

public class TransactionPagedResponse extends PagedResultDTO<TransactionResponseDTO> {

    public TransactionPagedResponse(List<TransactionResponseDTO> items, boolean hasNext,
            Long lastId) {
        super(items, hasNext, lastId);
    }
}
