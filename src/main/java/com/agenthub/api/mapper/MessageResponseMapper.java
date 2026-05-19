package com.agenthub.api.mapper;

import com.agenthub.api.dto.MessageResponse;
import com.agenthub.domain.model.agent.ChatMessage;

/**
 * 消息响应映射器。
 */
public final class MessageResponseMapper {

    private MessageResponseMapper() {
    }

    public static MessageResponse toResponse(ChatMessage message) {
        return new MessageResponse(
            message.getId(),
            message.getSessionId(),
            message.getRole(),
            message.getContent(),
            message.getCreatedAt()
        );
    }
}
