package com.itjima_server.exception;

public class DuplicateUserFieldException extends RuntimeException {

    public DuplicateUserFieldException(String message) {
        super(message);
    }
}
