package com.itjima_server.exception.transaction;

import com.itjima_server.exception.common.NotFoundException;

public class NotFoundTransactionException extends NotFoundException {

    public NotFoundTransactionException(String message) {
        super(message);
    }
}
