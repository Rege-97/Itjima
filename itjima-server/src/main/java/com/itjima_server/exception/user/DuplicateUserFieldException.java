package com.itjima_server.exception.user;

public class DuplicateUserFieldException extends RuntimeException {

    public DuplicateUserFieldException(String message) {
        super(message);
    }
}
