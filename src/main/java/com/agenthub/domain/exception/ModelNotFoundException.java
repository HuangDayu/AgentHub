package com.agenthub.domain.exception;

/**
 * 模型配置未找到异常。
 */
public class ModelNotFoundException extends RuntimeException {

    public ModelNotFoundException(String message) {
        super(message);
    }
}
