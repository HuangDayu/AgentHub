package com.agenthub.application.usecase;

import com.agenthub.domain.enums.AgentConfigCategory;
import com.agenthub.domain.enums.AgentConfigType;
import com.agenthub.domain.model.agent.AgentConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统提示词构建用例，动态注入 Agent 可用资源到系统提示词中。
 */
@Component
@RequiredArgsConstructor
public class SystemPromptBuilderUseCase {

    public String enrichPrompt(String basePrompt, List<AgentConfig> configs) {
        if (configs == null || configs.isEmpty()) return basePrompt;
        String resourceSection = buildResourceSection(configs);
        if (resourceSection.isBlank()) return basePrompt;
        return basePrompt + "\n\n" + resourceSection;
    }

    private String buildResourceSection(List<AgentConfig> configs) {
        StringBuilder sb = new StringBuilder();
        sb.append("## 你当前可用的资源\n\n");
        sb.append(buildToolSection(configs));
        sb.append(buildModelSection(configs));
        sb.append(buildKnowledgeSection(configs));
        return sb.toString();
    }

    private String buildToolSection(List<AgentConfig> configs) {
        List<AgentConfig> tools = filterByCategory(configs, AgentConfigCategory.TOOL);
        if (tools.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        sb.append("### 工具（").append(tools.size()).append("个）\n");
        for (AgentConfig t : tools) {
            sb.append("- ").append(t.getName());
            if (t.getDescription() != null && !t.getDescription().isBlank()) {
                sb.append(": ").append(truncate(t.getDescription(), 50));
            }
            sb.append("\n");
        }
        return sb.append("\n").toString();
    }

    private String buildModelSection(List<AgentConfig> configs) {
        List<AgentConfig> models = filterByCategory(configs, AgentConfigCategory.MODEL);
        if (models.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        sb.append("### 模型（").append(models.size()).append("个）\n");
        for (AgentConfig m : models) {
            sb.append("- ").append(m.getName());
            sb.append(" [").append(m.getType()).append("]\n");
        }
        return sb.append("\n").toString();
    }

    private String buildKnowledgeSection(List<AgentConfig> configs) {
        List<AgentConfig> kbs = filterByType(configs, AgentConfigType.KNOWLEDGE_BASE);
        if (kbs.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        sb.append("### 知识库（").append(kbs.size()).append("个）\n");
        for (AgentConfig kb : kbs) {
            sb.append("- ").append(kb.getName());
            if (kb.getDescription() != null && !kb.getDescription().isBlank()) {
                sb.append(": ").append(truncate(kb.getDescription(), 50));
            }
            sb.append("\n");
        }
        return sb.append("\n").toString();
    }

    private List<AgentConfig> filterByCategory(List<AgentConfig> configs, AgentConfigCategory category) {
        return configs.stream()
                .filter(c -> c.getCategory() == category && c.isEnabled())
                .collect(Collectors.toList());
    }

    private List<AgentConfig> filterByType(List<AgentConfig> configs, AgentConfigType type) {
        return configs.stream()
                .filter(c -> c.getType() == type && c.isEnabled())
                .collect(Collectors.toList());
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() > maxLen ? text.substring(0, maxLen) + "..." : text;
    }
}
