package com.agenthub.domain.exception;

/**
 * 危险操作拦截异常 - 403
 */
public class DangerousOperationBlockedException extends RuntimeException {
    public DangerousOperationBlockedException(String message) {
        super(message);
    }
}
