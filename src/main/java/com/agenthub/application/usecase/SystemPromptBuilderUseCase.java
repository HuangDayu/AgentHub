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

    /**
     * 构建工具区域。
     */
    private String buildToolSection(List<AgentConfig> configs) {
        List<AgentConfig> tools = filterByCategory(configs, AgentConfigCategory.TOOL);
        if (tools.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(buildSectionHeader("工具", tools.size()));
        tools.forEach(t -> sb.append(buildDescriptionLine(t)));
        return sb.append("\n").toString();
    }

    /**
     * 构建模型区域。
     */
    private String buildModelSection(List<AgentConfig> configs) {
        List<AgentConfig> models = filterByCategory(configs, AgentConfigCategory.MODEL);
        if (models.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(buildSectionHeader("模型", models.size()));
        models.forEach(m -> sb.append(buildModelLine(m)));
        return sb.append("\n").toString();
    }

    /**
     * 构建知识库区域。
     */
    private String buildKnowledgeSection(List<AgentConfig> configs) {
        List<AgentConfig> kbs = filterByType(configs, AgentConfigType.KNOWLEDGE_BASE);
        if (kbs.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(buildSectionHeader("知识库", kbs.size()));
        kbs.forEach(kb -> sb.append(buildDescriptionLine(kb)));
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

    /**
     * 构建区域标题行。
     */
    private String buildSectionHeader(String prefix, int count) {
        return "### " + prefix + "（" + count + "个）\n";
    }

    /**
     * 构建带描述的单行条目。
     */
    private String buildDescriptionLine(AgentConfig config) {
        String line = "- " + config.getName();
        if (config.getDescription() != null && !config.getDescription().isBlank()) {
            line += ": " + truncate(config.getDescription(), 50);
        }
        return line + "\n";
    }

    /**
     * 构建带类型的单行条目。
     */
    private String buildModelLine(AgentConfig config) {
        return "- " + config.getName() + " [" + config.getType() + "]\n";
    }
}
