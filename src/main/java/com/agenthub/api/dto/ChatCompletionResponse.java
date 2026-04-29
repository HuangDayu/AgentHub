package com.agenthub.api.dto;

import com.agenthub.domain.model.ChatMessage;

/**
 * 聊天补全响应体。
 */
public record ChatCompletionResponse(
        String id,
        String model,
        ChatMessage message,
        Usage usage
) {
    public record Usage(int promptTokens, int completionTokens, int totalTokens) {
    }
}
