package com.agenthub.application.port.out.agent;

import com.agenthub.application.command.AgentChatCommand;
import com.agenthub.domain.model.agent.AgentMessage;
import reactor.core.publisher.Flux;

/**
 * Agent端口接口，定义Agent的核心操作。
 *
 * @author huangdayu
 */
public interface AgentChatPort {


    Flux<AgentMessage> streamMessages(AgentChatCommand agentChatCommand);


    AgentMessage chatMessages(AgentChatCommand agentChatCommand);

    /**
     * 中断Agent执行。
     */
    void interrupt(String agentId, String sessionId);
}
