package com.agenthub.api.dto;

import com.agenthub.domain.model.ChatMessage;

import java.util.List;

/**
 * 聊天补全请求体。
 */
public record ChatCompletionRequest(
        List<ChatMessage> messages,
        String model,
        Boolean stream
) {
    /**
     * 构造非流式请求。
     */
    public ChatCompletionRequest(List<ChatMessage> messages, String model) {
        this(messages, model, false);
    }
}
