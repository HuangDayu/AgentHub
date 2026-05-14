package com.agenthub.domain.exception;

/** 资源未找到异常，当请求的资源不存在时抛出。 */
public class NotFoundException extends RuntimeException {

    /**
     * 构造未找到异常。
     *
     * @param message 异常消息
     */
    public NotFoundException(String message) {
        super(message);
    }
}
