package com.agenthub.application.usecase;

import com.agenthub.application.dto.AgentConfigTypeOutput;
import com.agenthub.application.dto.AvailableConfigOutput;
import com.agenthub.application.port.out.repositories.*;
import com.agenthub.domain.model.AgentConfig;
import com.agenthub.domain.model.AgentConfigCategory;
import com.agenthub.domain.model.AgentConfigType;
import com.agenthub.domain.model.ModelType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AgentConfigTypeUseCase {
    private final RetrievalStrategyRepository retrievalStrategyRepository;
    private final ModelStrategyRepository modelStrategyRepository;
    private final ToolStrategyRepository toolStrategyRepository;
    private final GuardrailStrategyRepository guardrailStrategyRepository;
    private final ModelConfigRepository modelConfigRepository;
    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final McpToolRepository mcpToolRepository;
    private final PromptTemplateRepository templateRepository;
    private final SkillRepository skillRepository;
    private final SystemToolsRepository systemToolsRepository;
    private final HttpToolRepository httpToolRepository;

    public List<AvailableConfigOutput> getAvailableConfigs(String category, String type, String workspaceId) {
        if (workspaceId == null) return List.of();
        AgentConfigType value = AgentConfigType.valueOf(type);
        return switch (AgentConfigCategory.valueOf(category)) {
            case STRATEGY -> switch (value) {
                case RETRIEVAL_STRATEGY -> getRetrievalStrategies(workspaceId);
                case MODEL_STRATEGY -> getModelStrategies(workspaceId);
                case TOOL_STRATEGY -> getToolStrategies(workspaceId);
                case GUARDRAIL_STRATEGY -> getGuardrailStrategies(workspaceId);
                default -> List.of();
            };
            case TOOL -> switch (value) {
                case MCP_TOOL -> getMcpTools(workspaceId);
                case SKILL_TOOL -> getSkillTools(workspaceId);
                case SYSTEM_TOOL -> getSystemTools(workspaceId);
                case HTTP_TOOL -> getHttpTools(workspaceId);
                default -> List.of();
            };
            case PROMPT -> switch (value) {
                case SYSTEM_PROMPT -> getPrompts(workspaceId, "system");
                case ASSISTANT_PROMPT -> getPrompts(workspaceId, "assistant");
                default -> List.of();
            };
            case MODEL -> switch (value) {
                case CHAT_MODEL -> getModelConfigs(workspaceId, ModelType.CHAT);
                case EMBEDDING_MODEL -> getModelConfigs(workspaceId, ModelType.EMBEDDING);
                default -> List.of();
            };
            case KNOWLEDGE -> getKnowledgeBases(workspaceId);
        };
    }


    private List<AvailableConfigOutput> getHttpTools(String workspaceId) {
        return httpToolRepository.findByWorkspaceId(workspaceId).stream()
                .map(v -> new AvailableConfigOutput(v.getId(), v.getName(), v.getDescription())).toList();
    }

    private List<AvailableConfigOutput> getSystemTools(String workspaceId) {
        return systemToolsRepository.findByWorkspaceId(workspaceId).stream()
                .map(v -> new AvailableConfigOutput(v.getId(), v.getToolName(), v.getDescription())).toList();
    }

    private List<AvailableConfigOutput> getSkillTools(String workspaceId) {
        return skillRepository.findByWorkspaceId(workspaceId).stream()
                .map(v -> new AvailableConfigOutput(v.getId(), v.getName(), v.getDescription())).toList();
    }

    private List<AvailableConfigOutput> getPrompts(String workspaceId, String category) {
        return templateRepository.findByWorkspaceIdAndCategory(workspaceId, category).stream()
                .map(v -> new AvailableConfigOutput(v.getId(), v.getName(), v.getDescription())).toList();
    }

    private List<AvailableConfigOutput> getMcpTools(String workspaceId) {
        return mcpToolRepository.findByWorkspaceId(workspaceId).stream()
                .map(v -> new AvailableConfigOutput(v.getId(), v.getName(), v.getDescription())).toList();
    }

    private List<AvailableConfigOutput> getKnowledgeBases(String workspaceId) {
        return knowledgeBaseRepository.findByWorkspace(workspaceId).stream()
                .map(kb -> new AvailableConfigOutput(kb.getId(), kb.getName(), kb.getDescription())).toList();
    }

    private List<AvailableConfigOutput> getRetrievalStrategies(String workspaceId) {
        return retrievalStrategyRepository.findByWorkspace(workspaceId).stream()
                .map(s -> new AvailableConfigOutput(s.getId(), s.getName(), s.getDescription())).toList();
    }

    private List<AvailableConfigOutput> getModelStrategies(String workspaceId) {
        return modelStrategyRepository.findByWorkspace(workspaceId).stream()
                .map(s -> new AvailableConfigOutput(s.getId(), s.getName(), s.getDescription())).toList();
    }

    private List<AvailableConfigOutput> getToolStrategies(String workspaceId) {
        return toolStrategyRepository.findByWorkspace(workspaceId).stream()
                .map(s -> new AvailableConfigOutput(s.getId(), s.getName(), s.getDescription())).toList();
    }

    private List<AvailableConfigOutput> getGuardrailStrategies(String workspaceId) {
        return guardrailStrategyRepository.findByWorkspace(workspaceId).stream()
                .map(s -> new AvailableConfigOutput(s.getId(), s.getName(), s.getDescription())).toList();
    }

    private List<AvailableConfigOutput> getModelConfigs(String workspaceId, ModelType modelType) {
        return modelConfigRepository.findByWorkspace(workspaceId, modelType.name()).stream()
                .map(m -> new AvailableConfigOutput(m.id(), m.name(), null)).toList();
    }

    public List<AgentConfigTypeOutput> getConfigTypes() {
        return Arrays.stream(AgentConfigCategory.values()).map(v -> {
            List<AgentConfigTypeOutput.TypeInfo> list = Arrays.stream(v.getTypes())
                    .map(t -> new AgentConfigTypeOutput.TypeInfo(t.name(), t.getDisplayName(), t.getDescription())).toList();
            return new AgentConfigTypeOutput(v.name(), v.getDisplayName(), v.getDescription(), list);
        }).toList();
    }


}
