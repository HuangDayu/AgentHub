package com.agenthub.application.dto;



/**
 * 模型测试结果。
 */
public record ModelTestOutput(
        boolean success,
        String message,
        String details
) {}
