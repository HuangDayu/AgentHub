package com.agenthub.infrastructure.agents.aliyun.subagent;

import io.agentscope.harness.agent.subagent.SubagentDeclaration;
import lombok.AllArgsConstructor;
import lombok.Data;
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

    public SubagentDeclaration createSubagentWithWorkspace(SubagentSpec spec) {
        return SubagentDeclaration.builder()
                .name(spec.getName())
                .description(spec.getDescription())
                .workspace(spec.getWorkspace())
                .maxIters(spec.getMaxIters())
                .build();
    }

    public SubagentDeclaration createSubagentWithTools(SubagentSpec spec) {
        return SubagentDeclaration.builder()
                .name(spec.getName())
                .description(spec.getDescription())
                .maxIters(spec.getMaxIters())
                .tools(spec.getTools())
                .build();
    }

    @Data
    @AllArgsConstructor
    public static class SubagentSpec {
        private String name;
        private String description;
        private int maxIters;
        private Path workspace;
        private List<String> tools;
        private String url;
    }

    public SubagentDeclaration createRemoteSubagent(String name, String description, String url) {
        return SubagentDeclaration.builder()
                .name(name)
                .description(description)
                .url(url)
                .build();
    }
}
