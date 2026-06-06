package com.agenthub.domain.exception;

/**
 * 速率限制异常 - 429
 */
public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException(String message) {
        super(message);
    }
}
