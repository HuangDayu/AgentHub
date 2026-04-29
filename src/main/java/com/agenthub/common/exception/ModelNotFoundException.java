package com.agenthub.common.exception;

/**
 * 模型配置未找到异常。
 */
public class ModelNotFoundException extends RuntimeException {

    public ModelNotFoundException(String message) {
        super(message);
    }
}
