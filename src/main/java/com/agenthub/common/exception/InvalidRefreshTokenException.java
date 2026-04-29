package com.agenthub.common.exception;

/**
 * 无效刷新令牌异常.
 * <p>
 * 当刷新令牌不存在、已过期或已被撤销时抛出此异常。
 * </p>
 */
public class InvalidRefreshTokenException extends RuntimeException {
    /**
     * 构造无效刷新令牌异常。
     *
     * @param message 错误消息
     */
    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
