package com.itjima_server.exception.item;

import com.itjima_server.exception.common.NotFoundException;

public class NotFoundItemException extends NotFoundException {

    public NotFoundItemException(String message) {
        super(message);
    }
}
