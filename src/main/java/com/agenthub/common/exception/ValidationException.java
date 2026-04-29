package com.agenthub.common.exception;

/**
 * 参数验证异常。
 * <p>
 * 当输入参数不符合业务规则时抛出此异常。
 * </p>
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
