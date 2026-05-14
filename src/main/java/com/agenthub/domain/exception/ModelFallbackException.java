package com.agenthub.domain.exception;

/**
 * 模型降级异常。
 * <p>
 * 当降级链中所有备选模型都不可用时抛出此异常。
 * </p>
 */
public class ModelFallbackException extends RuntimeException {

    /**
     * 构造降级异常。
     *
     * @param message 异常消息
     */
    public ModelFallbackException(String message) {
        super(message);
    }

    /**
     * 构造带原因的降级异常。
     *
     * @param message 异常消息
     * @param cause   原始异常
     */
    public ModelFallbackException(String message, Throwable cause) {
        super(message, cause);
    }
}
