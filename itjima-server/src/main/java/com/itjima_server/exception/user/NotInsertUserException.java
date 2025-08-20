package com.itjima_server.exception.user;

import com.itjima_server.exception.common.NotInsertException;

public class NotInsertUserException extends NotInsertException {

    public NotInsertUserException(String message) {
        super(message);
    }
}
