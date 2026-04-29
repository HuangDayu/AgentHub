package com.agenthub.application.usecase;

import com.agenthub.application.dto.AgentConfigTypeOutput;
import com.agenthub.application.dto.AvailableConfigOutput;
import com.agenthub.application.port.out.repositories.*;
import com.agenthub.application.port.out.repositories.*;
import com.agenthub.domain.model.AgentConfig;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.agenthub.domain.model.AgentConfig.Category.*;
import static com.agenthub.domain.model.AgentConfig.Type.*;

@Service
public class AgentConfigTypeUseCase {
    private final RetrievalStrategyRepository retrievalStrategyRepository;
    private final ModelStrategyRepository modelStrategyRepository;
    private final ToolStrategyRepository toolStrategyRepository;
    private final GuardrailStrategyRepository guardrailStrategyRepository;
    private final ModelConfigRepository modelConfigRepository;
    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final McpToolRepository mcpToolRepository;
    private final PromptTemplateRepository templateRepository;

    public AgentConfigTypeUseCase(
            RetrievalStrategyRepository retrievalStrategyRepository,
            ModelStrategyRepository modelStrategyRepository,
            ToolStrategyRepository toolStrategyRepository,
            GuardrailStrategyRepository guardrailStrategyRepository,
            ModelConfigRepository modelConfigRepository, KnowledgeBaseRepository knowledgeBaseRepository, McpToolRepository mcpToolRepository, PromptTemplateRepository templateRepository) {
        this.retrievalStrategyRepository = retrievalStrategyRepository;
        this.modelStrategyRepository = modelStrategyRepository;
        this.toolStrategyRepository = toolStrategyRepository;
        this.guardrailStrategyRepository = guardrailStrategyRepository;
        this.modelConfigRepository = modelConfigRepository;
        this.knowledgeBaseRepository = knowledgeBaseRepository;
        this.mcpToolRepository = mcpToolRepository;
        this.templateRepository = templateRepository;
    }

    public List<AvailableConfigOutput> getAvailableConfigs(String category, String type, String workspaceId) {
        if (workspaceId == null) return List.of();
        AgentConfig.Type value = AgentConfig.Type.valueOf(type);
        return switch (AgentConfig.Category.valueOf(category)) {
            case STRATEGY -> switch (value) {
                case RETRIEVAL_STRATEGY -> getRetrievalStrategies(workspaceId);
                case MODEL_STRATEGY -> getModelStrategies(workspaceId);
                case TOOL_STRATEGY -> getToolStrategies(workspaceId);
                case GUARDRAIL_STRATEGY -> getGuardrailStrategies(workspaceId);
                default -> List.of();
            };
            case TOOL -> switch (value) {
                case MCP_TOOL -> getMcpTools(workspaceId);
                default -> List.of();
            };
            case PROMPT -> switch (value) {
                case SYSTEM_PROMPT -> getSystemPrompts(workspaceId);
                default -> List.of();
            };
            case MODEL -> switch (value) {
                case CHAT_MODEL -> getModelConfigs(workspaceId);
                case EMBEDDING_MODEL -> getModelConfigs(workspaceId);
                default -> List.of();
            };
            case KNOWLEDGE -> getKnowledgeBases(workspaceId);
        };
    }

    private List<AvailableConfigOutput> getSystemPrompts(String workspaceId) {
        return templateRepository.findByWorkspaceId(workspaceId).stream()
                .map(kb -> new AvailableConfigOutput(kb.id(), kb.name(), kb.description())).toList();
    }

    private List<AvailableConfigOutput> getMcpTools(String workspaceId) {
        return mcpToolRepository.findByWorkspaceId(workspaceId).stream()
                .map(kb -> new AvailableConfigOutput(kb.id(), kb.name(), kb.description())).toList();
    }

    private List<AvailableConfigOutput> getKnowledgeBases(String workspaceId) {
        return knowledgeBaseRepository.findByWorkspace(workspaceId).stream()
                .map(kb -> new AvailableConfigOutput(kb.id(), kb.name(), kb.description())).toList();
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

    private List<AvailableConfigOutput> getModelConfigs(String workspaceId) {
        return modelConfigRepository.findByWorkspace(workspaceId).stream()
                .map(m -> new AvailableConfigOutput(m.id(), m.name(), null)).toList();
    }

    public List<AgentConfigTypeOutput> getConfigTypes() {
        return List.of(buildStrategyCategory(), buildToolCategory(), buildPromptCategory(), buildModelCategory(), buildKnowledgeCategory());
    }

    private AgentConfigTypeOutput buildStrategyCategory() {
        return new AgentConfigTypeOutput(STRATEGY.name(), "策略配置", "Agent的策略相关配置",
                List.of(new AgentConfigTypeOutput.TypeInfo(RETRIEVAL_STRATEGY.name(), "检索策略", "知识检索策略配置"),
                        new AgentConfigTypeOutput.TypeInfo(MODEL_STRATEGY.name(), "模型策略", "模型调用策略配置"),
                        new AgentConfigTypeOutput.TypeInfo(TOOL_STRATEGY.name(), "工具策略", "工具调用策略配置"),
                        new AgentConfigTypeOutput.TypeInfo(GUARDRAIL_STRATEGY.name(), "护栏策略", "输入输出护栏策略")));
    }

    private AgentConfigTypeOutput buildToolCategory() {
        return new AgentConfigTypeOutput(TOOL.name(), "工具配置", "Agent的工具相关配置",
                List.of(new AgentConfigTypeOutput.TypeInfo(MCP_TOOL.name(), "MCP工具", "MCP协议工具配置")));
    }

    private AgentConfigTypeOutput buildPromptCategory() {
        return new AgentConfigTypeOutput(PROMPT.name(), "提示词配置", "Agent的提示词相关配置",
                List.of(new AgentConfigTypeOutput.TypeInfo(SYSTEM_PROMPT.name(), "系统提示词", "Agent系统提示词模板")));
    }

    private AgentConfigTypeOutput buildModelCategory() {
        return new AgentConfigTypeOutput(MODEL.name(), "模型配置", "Agent的模型相关配置",
                List.of(new AgentConfigTypeOutput.TypeInfo(CHAT_MODEL.name(), "聊天模型", "对话模型配置"),
                        new AgentConfigTypeOutput.TypeInfo(EMBEDDING_MODEL.name(), "嵌入模型", "向量嵌入模型配置")));
    }

    private AgentConfigTypeOutput buildKnowledgeCategory() {
        return new AgentConfigTypeOutput(KNOWLEDGE.name(), "知识库", "Agent的知识库相关配置",
                List.of(new AgentConfigTypeOutput.TypeInfo(KNOWLEDGE_BASE.name(), "知识库", "知识库配置")));
    }


}
