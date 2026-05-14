package com.agenthub.domain.exception;

/**
 * 资源冲突异常。
 * <p>
 * 当创建的资源已存在时抛出此异常。
 * </p>
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
