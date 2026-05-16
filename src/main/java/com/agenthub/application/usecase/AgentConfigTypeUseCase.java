package com.agenthub.application.usecase;

import com.agenthub.application.dto.AgentConfigTypeOutput;
import com.agenthub.application.dto.AvailableConfigOutput;
import com.agenthub.application.port.out.repositories.*;
import com.agenthub.domain.model.AgentConfig;
import com.agenthub.domain.model.AgentConfigCategory;
import com.agenthub.domain.model.AgentConfigType;
import com.agenthub.domain.model.ModelType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;

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

    private Map<AgentConfigCategory, Map<AgentConfigType, Function<String, List<AvailableConfigOutput>>>> dispatchMap;

    @PostConstruct
    public void init() {
        this.dispatchMap = Map.of(
            AgentConfigCategory.STRATEGY, strategyDispatch(),
            AgentConfigCategory.TOOL, toolDispatch(),
            AgentConfigCategory.PROMPT, promptDispatch(),
            AgentConfigCategory.MODEL, modelDispatch()
        );
    }

    public List<AvailableConfigOutput> getAvailableConfigs(String category, String type, String workspaceId) {
        if (workspaceId == null) return List.of();
        var categoryDispatcher = dispatchMap.get(AgentConfigCategory.valueOf(category));
        if (categoryDispatcher == null) {
            return getKnowledgeBases(workspaceId);
        }
        var handler = categoryDispatcher.get(AgentConfigType.valueOf(type));
        return handler != null ? handler.apply(workspaceId) : List.of();
    }

    private Map<AgentConfigType, Function<String, List<AvailableConfigOutput>>> strategyDispatch() {
        return Map.of(
            AgentConfigType.RETRIEVAL_STRATEGY, this::getRetrievalStrategies,
            AgentConfigType.MODEL_STRATEGY, this::getModelStrategies,
            AgentConfigType.TOOL_STRATEGY, this::getToolStrategies,
            AgentConfigType.GUARDRAIL_STRATEGY, this::getGuardrailStrategies
        );
    }

    private Map<AgentConfigType, Function<String, List<AvailableConfigOutput>>> toolDispatch() {
        return Map.of(
            AgentConfigType.MCP_TOOL, this::getMcpTools,
            AgentConfigType.SKILL_TOOL, this::getSkillTools,
            AgentConfigType.SYSTEM_TOOL, this::getSystemTools,
            AgentConfigType.HTTP_TOOL, this::getHttpTools
        );
    }

    private Map<AgentConfigType, Function<String, List<AvailableConfigOutput>>> promptDispatch() {
        return Map.of(
            AgentConfigType.SYSTEM_PROMPT, w -> getPrompts(w, "system"),
            AgentConfigType.ASSISTANT_PROMPT, w -> getPrompts(w, "assistant")
        );
    }

    private Map<AgentConfigType, Function<String, List<AvailableConfigOutput>>> modelDispatch() {
        return Map.of(
            AgentConfigType.CHAT_MODEL, w -> getModelConfigs(w, ModelType.CHAT),
            AgentConfigType.EMBEDDING_MODEL, w -> getModelConfigs(w, ModelType.EMBEDDING)
        );
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
                .map(m -> new AvailableConfigOutput(m.getId(), m.getName(), null)).toList();
    }

    public List<AgentConfigTypeOutput> getConfigTypes() {
        return Arrays.stream(AgentConfigCategory.values()).map(v -> {
            List<AgentConfigTypeOutput.TypeInfo> list = Arrays.stream(v.getTypes())
                    .map(t -> new AgentConfigTypeOutput.TypeInfo(t.name(), t.getDisplayName(), t.getDescription())).toList();
            return new AgentConfigTypeOutput(v.name(), v.getDisplayName(), v.getDescription(), list);
        }).toList();
    }


}
