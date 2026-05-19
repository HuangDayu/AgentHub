package com.agenthub.api.dto;

import com.agenthub.domain.model.agent.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 聊天补全请求体。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatCompletionRequest {
    private List<ChatMessage> messages;
    private String model;
    private Boolean stream;

    /**
     * 构造非流式请求。
     */
    public ChatCompletionRequest(List<ChatMessage> messages, String model) {
        this.messages = messages;
        this.model = model;
        this.stream = false;
    }
}
