package com.itjima_server.exception.common;

public class NotAuthorException extends RuntimeException {

    public NotAuthorException(String message) {
        super(message);
    }
}
