package com.agenthub.application.dto;

/**
 * 入库流水线异常，封装流水线执行过程中的错误。
 */
public class IngestionPipelineError extends RuntimeException {
    /**
     * 创建流水线异常。
     *
     * @param message 异常消息
     * @param cause   原始异常
     */
    public IngestionPipelineError(String message, Throwable cause) {
        super(message, cause);
    }
}
