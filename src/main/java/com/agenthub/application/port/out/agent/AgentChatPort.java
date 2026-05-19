package com.agenthub.application.port.out.agent;

import com.agenthub.domain.model.AgentMessage;
import reactor.core.publisher.Flux;

/**
 * Agent端口接口，定义Agent的核心操作。
 *
 * @author huangdayu
 */
public interface AgentChatPort {

    /**
     * 流式对话。
     */
    Flux<AgentMessage> streamMessages(String agentId, String sessionId, String userMessage);

    /**
     * 同步对话。
     */
    AgentMessage chatMessages(String agentId, String sessionId, String userMessage);

    /**
     * 中断Agent执行。
     */
    void interrupt(String agentId, String sessionId);
}
