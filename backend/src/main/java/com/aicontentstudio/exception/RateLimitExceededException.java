package com.aicontentstudio.exception;

import org.springframework.http.HttpStatus;

public class RateLimitExceededException extends ApiException {

    public RateLimitExceededException(int limit) {
        super("AI request limit of " + limit + " per day exceeded. Please try again tomorrow.",
              HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_EXCEEDED");
    }
}
