package com.agenthub.infrastructure.agents.aliyun.workspace;

import io.agentscope.harness.agent.workspace.WorkspaceManager;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

/**
 * AgentScope 工作区管理工厂。
 */
@Component
public class WorkspaceManagerFactory {

    public WorkspaceManager create(Path workspacePath) {
        return new WorkspaceManager(workspacePath);
    }
}
