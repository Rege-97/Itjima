package com.itjima_server.dto.item.response;

import com.itjima_server.domain.item.ItemStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemCountDTO {

    private ItemStatus status;
    private int count;
}
