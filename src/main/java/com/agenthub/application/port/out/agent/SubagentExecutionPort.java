package com.agenthub.application.port.out.agent;

import com.agenthub.application.command.SubAgentChatCommand;
import com.agenthub.domain.model.agent.AgentMessage;
import reactor.core.publisher.Flux;

/**
 * 子Agent执行端口，应用层依赖此接口而非直接依赖基础设施。
 */
public interface SubagentExecutionPort {

    /**
     * 执行Subagent对话并保存消息。
     *
     * @return 消息流
     */
    Flux<AgentMessage> stream(SubAgentChatCommand subAgentChatCommand);


}
