package com.agenthub.common.exception;

/**
 * 策略未找到异常。
 *
 * <p>当根据策略 ID 查询不到对应记录时抛出。</p>
 */
public class PolicyNotFoundException extends RuntimeException {

    public PolicyNotFoundException(String message) {
        super(message);
    }
}
