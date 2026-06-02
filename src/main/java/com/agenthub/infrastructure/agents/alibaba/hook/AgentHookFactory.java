package com.agenthub.infrastructure.agents.alibaba.hook;

import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.domain.model.agent.ReActAgentWorkspace;
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

    /** 日志 Hook。 */
    public AgentHook loggingHook() {
        return new LoggingHook();
    }

    /**
     * 模型策略 Hook。
     *
     * @param context Agent 上下文
     * @return 模型策略 Hook
     */
    public ModelStrategyHook modelStrategyHook(ReActAgentContext context) {
        return new ModelStrategyHook(context);
    }

    /** 技能 Hook。 */
    public SkillsAgentHook skillsAgentHook(ReActAgentWorkspace workspace) {
        return SkillsAgentHook.builder()
                .skillRegistry(FileSystemSkillRegistry.builder()
                        .projectSkillsDirectory(workspace.getSkillsPath().toString())
                        .userSkillsDirectory(workspace.getShareSkillsPath().toString())
                        .build())
                .autoReload(false)
                .build();
    }

    /** Shell 工具 Hook。 */
    public ShellToolAgentHook shellToolAgentHook(ReActAgentWorkspace workspace) {
        return ShellToolAgentHook.builder()
                .shellTool2(ShellTool2.builder(workspace.getRootPath().toString()).build())
                .build();
    }
}
