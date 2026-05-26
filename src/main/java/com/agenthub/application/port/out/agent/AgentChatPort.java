package com.agenthub.application.port.out.agent;

import com.agenthub.domain.model.agent.AgentMessage;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Agent端口接口，定义Agent的核心操作。
 *
 * @author huangdayu
 */
public interface AgentChatPort {

    /**
     * 流式对话。
     */
    default Flux<AgentMessage> streamMessages(String agentId, String sessionId, String userMessage) {
        return streamMessages(agentId, sessionId, userMessage, List.of());
    }

    Flux<AgentMessage> streamMessages(String agentId, String sessionId, String userMessage, List<String> filePaths);

    /**
     * 同步对话。
     */
    default AgentMessage chatMessages(String agentId, String sessionId, String userMessage) {
        return chatMessages(agentId, sessionId, userMessage, List.of());
    }

    AgentMessage chatMessages(String agentId, String sessionId, String userMessage, List<String> filePaths);

    /**
     * 中断Agent执行。
     */
    void interrupt(String agentId, String sessionId);
}
