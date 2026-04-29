package com.agenthub.common.exception;

/**
 * 访问拒绝异常.
 * <p>
 * 当用户没有足够权限访问资源时抛出。
 * </p>
 */
public class AccessDeniedException extends RuntimeException {
    
    public AccessDeniedException(String message) {
        super(message);
    }
    
    public AccessDeniedException(String userId, String permission) {
        super("User '" + userId + "' does not have permission: " + permission);
    }
}
