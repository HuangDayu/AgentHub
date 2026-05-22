package com.agenthub.infrastructure.agents.aliyun.subagent;

import io.agentscope.harness.agent.subagent.SubagentDeclaration;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

/**
 * AgentScope 子Agent管理工厂。
 */
@Component
public class SubagentDeclarationFactory {

    public SubagentDeclaration createSubagent(String name, String description, int maxIters) {
        return SubagentDeclaration.builder()
                .name(name)
                .description(description)
                .maxIters(maxIters)
                .build();
    }

    public SubagentDeclaration createSubagentWithWorkspace(String name, String description, Path workspace, int maxIters) {
        return SubagentDeclaration.builder()
                .name(name)
                .description(description)
                .workspace(workspace)
                .maxIters(maxIters)
                .build();
    }

    public SubagentDeclaration createSubagentWithTools(String name, String description, int maxIters, List<String> tools) {
        return SubagentDeclaration.builder()
                .name(name)
                .description(description)
                .maxIters(maxIters)
                .tools(tools)
                .build();
    }

    public SubagentDeclaration createRemoteSubagent(String name, String description, String url) {
        return SubagentDeclaration.builder()
                .name(name)
                .description(description)
                .url(url)
                .build();
    }
}
