package com.agenthub.domain.model;

import java.util.List;

public record SessionMessage(
        String sessionId,
        String agentId,
        String chatModelConfigId,
        ModelStrategy strategy,
        List<ChatMessage> chatMessages
) {
}
