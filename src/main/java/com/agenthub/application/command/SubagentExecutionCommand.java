package com.agenthub.application.command;

import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.domain.model.agent.Subagent;
import com.agenthub.domain.model.agent.Subsession;
import lombok.Data;

/**
 * 子Agent执行命令。
 */
@Data
public class SubagentExecutionCommand {
    private Subagent subagent;
    private Subsession subsession;
    private ReActAgentContext context;
    private String input;
}
