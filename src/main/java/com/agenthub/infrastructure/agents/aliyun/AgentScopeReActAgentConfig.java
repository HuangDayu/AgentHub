package com.agenthub.infrastructure.agents.aliyun;

import com.agenthub.domain.model.agent.Agent;
import com.agenthub.domain.model.agent.ReActAgentWorkspace;
import io.agentscope.core.model.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.util.List;

/**
 * AgentScope Harness 框架的 Agent 配置持有类。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentScopeReActAgentConfig {

    private Agent agent;
    private Model model;
    private String systemPrompt;
    private Path workspacePath;
    private ReActAgentWorkspace workspace;

    private List<io.agentscope.core.tool.Tool> tools;
    private io.agentscope.core.memory.Memory memory;

}
