package com.aicontentstudio.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends ApiException {

    public UnauthorizedException(String message) {
        super(message, HttpStatus.FORBIDDEN, "ACCESS_DENIED");
    }
}
