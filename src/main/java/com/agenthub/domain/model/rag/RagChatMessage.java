package com.agenthub.domain.model.rag;

import java.util.List;

import com.agenthub.domain.model.agent.ChatMessage;
import com.agenthub.domain.model.strategy.ModelStrategy;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RagChatMessage {
    private String sessionId;
    private String agentId;
    private String chatModelConfigId;
    private ModelStrategy strategy;
    private List<ChatMessage> chatMessages;
}
