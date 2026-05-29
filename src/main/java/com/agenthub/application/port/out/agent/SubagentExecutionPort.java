package com.agenthub.application.port.out.agent;

import com.agenthub.application.command.SubAgentChatCommand;
import com.agenthub.application.command.SubagentExecutionCommand;
import com.agenthub.domain.model.agent.AgentMessage;
import com.agenthub.domain.model.agent.Subagent;
import reactor.core.publisher.Flux;

/**
 * 子Agent执行端口，应用层依赖此接口而非直接依赖基础设施。
 */
public interface SubagentExecutionPort {

    /**
     * 后台执行Subagent任务。
     */
    void execute(SubagentExecutionCommand command);

    /**
     * 流式执行Subagent对话并保存消息。
     */
    Flux<AgentMessage> stream(SubAgentChatCommand subAgentChatCommand);

    /**
     * 停止Subagent执行。
     */
    boolean stop(Subagent subagent, String subsessionId);
}
