package com.itjima_server.exception.agreement;

import com.itjima_server.exception.common.NotFoundException;

public class NotFoundAgreementException extends NotFoundException {

    public NotFoundAgreementException(String message) {
        super(message);
    }
}
