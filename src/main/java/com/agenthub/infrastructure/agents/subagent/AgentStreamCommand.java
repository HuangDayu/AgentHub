package com.agenthub.infrastructure.agents.subagent;

import com.agenthub.domain.model.agent.AbstractReActAgent;
import lombok.Data;

import java.util.List;

/**
 * Agent消息流执行命令。
 */
@Data
public class AgentStreamCommand {
    private AbstractReActAgent agent;
    private String sessionId;
    private String userMessage;
    private List<String> filePaths;
}
