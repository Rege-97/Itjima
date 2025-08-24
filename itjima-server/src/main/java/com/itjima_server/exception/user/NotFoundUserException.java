package com.itjima_server.exception.user;

import com.itjima_server.exception.common.NotFoundException;

public class NotFoundUserException extends NotFoundException {

    public NotFoundUserException(String message) {
        super(message);
    }
}
