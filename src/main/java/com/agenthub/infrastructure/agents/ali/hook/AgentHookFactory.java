package com.agenthub.infrastructure.agents.ali.hook;

import com.agenthub.domain.model.ReActAgentWorkspace;
import com.alibaba.cloud.ai.graph.agent.hook.AgentHook;
import com.alibaba.cloud.ai.graph.agent.hook.shelltool.ShellToolAgentHook;
import com.alibaba.cloud.ai.graph.agent.hook.skills.SkillsAgentHook;
import com.alibaba.cloud.ai.graph.agent.tools.ShellTool2;
import com.alibaba.cloud.ai.graph.skills.registry.filesystem.FileSystemSkillRegistry;
import org.springframework.stereotype.Component;

/**
 * Agent Hook工厂，提供常用Hook实例。
 */
@Component
public class AgentHookFactory {

    public AgentHook loggingHook() {
        return new LoggingHook();
    }

    public SkillsAgentHook skillsAgentHook(ReActAgentWorkspace workspace) {
        return SkillsAgentHook.builder()
                .skillRegistry(FileSystemSkillRegistry.builder()
                        .projectSkillsDirectory(workspace.getSkillsPath().toString())
                        .userSkillsDirectory(workspace.getShareSkillsPath().toString())
                        .build())
                .autoReload(false)
                .build();
    }

    public ShellToolAgentHook shellToolAgentHook(ReActAgentWorkspace workspace) {
        return ShellToolAgentHook.builder()
                .shellTool2(ShellTool2.builder(workspace.getRootPath().toString()).build())
                .build();
    }


}
