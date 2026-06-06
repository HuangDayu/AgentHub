package com.agenthub.domain.exception;

/**
 * 权限拒绝异常 - 403
 */
public class PermissionDeniedException extends RuntimeException {
    public PermissionDeniedException(String message) {
        super(message);
    }
}
