package com.agenthub.domain.model;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionMessage {
    private String sessionId;
    private String agentId;
    private String chatModelConfigId;
    private ModelStrategy strategy;
    private List<ChatMessage> chatMessages;
}
