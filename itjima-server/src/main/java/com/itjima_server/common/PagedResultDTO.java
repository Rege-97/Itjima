package com.itjima_server.common;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PagedResultDTO<T> {

    private List<T> items;
    private boolean hasNext;
    private Long lastId;

    public static <T> PagedResultDTO<T> from(List<T> items, boolean hasNext, Long lastId) {
        return new PagedResultDTO<>(items, hasNext, lastId);
    }
}
