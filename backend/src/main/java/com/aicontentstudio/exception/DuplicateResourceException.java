package com.aicontentstudio.exception;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends ApiException {

    public DuplicateResourceException(String resource, String field, String value) {
        super(resource + " already exists with " + field + ": " + value,
              HttpStatus.CONFLICT, "DUPLICATE_RESOURCE");
    }
}
