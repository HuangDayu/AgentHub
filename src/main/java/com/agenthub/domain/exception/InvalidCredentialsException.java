package com.agenthub.domain.exception;

/**
 * 无效凭据异常.
 * <p>
 * 当用户提供的用户名或密码不正确时抛出此异常。
 * </p>
 */
public class InvalidCredentialsException extends RuntimeException {
    /**
     * 构造无效凭据异常。
     *
     * @param message 错误消息
     */
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
