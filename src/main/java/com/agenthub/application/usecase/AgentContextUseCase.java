package com.agenthub.application.usecase;

import com.agenthub.application.factory.AgentContextFactory;
import com.agenthub.application.port.out.repositories.*;
import com.agenthub.application.port.out.tools.ToolCallbackResolverPort;
import com.agenthub.domain.enums.AgentConfigCategory;
import com.agenthub.domain.enums.AgentConfigType;
import com.agenthub.domain.enums.AgentToolType;
import com.agenthub.domain.exception.NotFoundException;
import com.agenthub.domain.model.PromptTemplateInfo;
import com.agenthub.domain.model.Workspace;
import com.agenthub.domain.model.agent.*;
import com.agenthub.domain.model.strategy.GuardrailStrategy;
import com.agenthub.domain.model.strategy.ModelStrategy;
import com.agenthub.domain.model.strategy.RetrievalStrategy;
import com.agenthub.domain.model.strategy.ToolStrategy;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static com.agenthub.common.utils.TtlUtils.parallelStreamWithTtl;
import static com.agenthub.domain.enums.AgentToolType.*;

/**
 * @author huangdayu
 */
@Component
@RequiredArgsConstructor
public class AgentContextUseCase implements AgentContextFactory {
    private final AgentRepository agentRepository;
    private final AgentConfigRepository agentConfigRepository;
    private final PromptTemplateRepository promptTemplateRepository;
    private final ModelStrategyRepository modelStrategyRepository;
    private final RetrievalStrategyRepository retrievalStrategyRepository;
    private final ToolStrategyRepository toolStrategyRepository;
    private final GuardrailStrategyRepository guardrailStrategyRepository;
    private final WorkspaceRepository workspaceRepository;
    private final ToolCallbackResolverPort toolCallbackResolver;
    private final SystemPromptBuilderUseCase systemPromptBuilderUseCase;
    private final ToolFilterUseCase toolFilterUseCase;

    @Override
    public ReActAgentContext buildContext(String agentId, String sessionId) {
        Agent agent = agentRepository.findById(agentId).orElseThrow(() -> new NotFoundException("Agent not found: " + agentId));
        List<AgentConfig> configs = agentConfigRepository.findByAgentIdAndEnabled(agentId);
        return buildContext(agent, sessionId, configs, agent.getWorkspaceId());
    }


    private ReActAgentContext buildContext(Agent agent, String sessionId, List<AgentConfig> configs, String workspaceId) {
        List<AgentToolInfo> agentToolInfos = resolveTools(configs);
        ToolStrategy toolStrategy = resolveToolStrategy(configs);
        return ReActAgentContext.builder()
                .agent(agent)
                .sessionId(sessionId)
                .chatModelId(resolveChatModelId(configs))
                .systemPrompt(resolveSystemPrompt(configs))
                .toolInfos(agentToolInfos)
                .toolCallbacks(resolveToolCallbacks(agentToolInfos, toolStrategy))
                .knowledgeIds(resolveKnowledgeIds(configs))
                .modelStrategy(resolveModelStrategy(configs))
                .toolStrategy(toolStrategy)
                .guardrailStrategy(resolveGuardrailStrategy(configs))
                .retrievalStrategy(resolveRetrievalStrategy(configs))
                .workspace(resolveReActAgentWorkspace(workspaceId))
                .agentConfigs(configs)
                .build();
    }

    private ReActAgentWorkspace resolveReActAgentWorkspace(String workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new NotFoundException("Workspace not found: " + workspaceId));
        return ReActAgentWorkspace.builder()
                .workspace(workspace)
                .rootPath(resolvePath(workspace, ""))
                .agentsPath(resolvePath(workspace, "agents"))
                .cronPath(resolvePath(workspace, "cron"))
                .logsPath(resolvePath(workspace, "logs"))
                .configsPath(resolvePath(workspace, "configs"))
                .sessionsPath(resolvePath(workspace, "sessions"))
                .skillsPath(resolvePath(workspace, "skills"))
                .shareSkillsPath(defaultShareSkillPath())
                .build();
    }

    private Path defaultShareSkillPath() {
        return Paths.get(System.getProperty("user.home"), ".agents", "skills");
    }

    @SneakyThrows
    private Path resolvePath(Workspace workspace, String subPath) {
        Path path = Paths.get(System.getProperty("user.home"), ".agenthub", workspace.getWorkspaceCode(), subPath);
        Files.createDirectories(path);
        return path;
    }


    private String resolveChatModelId(List<AgentConfig> configs) {
        return findConfigId(configs, AgentConfigCategory.MODEL, AgentConfigType.CHAT_MODEL);
    }

    private String resolveSystemPrompt(List<AgentConfig> configs) {
        String promptId = findConfigId(configs, AgentConfigCategory.PROMPT, AgentConfigType.SYSTEM_PROMPT);
        String basePrompt = null;
        if (promptId != null) {
            basePrompt = promptTemplateRepository.findById(promptId)
                    .map(PromptTemplateInfo::getContent).orElse(null);
        }
        if (basePrompt == null) basePrompt = "";
        return systemPromptBuilderUseCase.enrichPrompt(basePrompt, configs);
    }

    private List<AgentToolInfo> resolveTools(List<AgentConfig> configs) {
        return configs.parallelStream().filter(c -> c.getCategory() == AgentConfigCategory.TOOL)
                .map(v -> new AgentToolInfo(valueOf(v.getType().name()), v.getConfigId(), v.getName(), v.getDescription(), v.isEnabled()))
                .toList();
    }


    private List<String> resolveToolIds(List<AgentConfig> configs) {
        return configs.stream()
                .filter(c -> c.getCategory() == AgentConfigCategory.TOOL)
                .map(AgentConfig::getConfigId)
                .toList();
    }

    private List<String> resolveKnowledgeIds(List<AgentConfig> configs) {
        return configs.stream()
                .filter(c -> c.getType() == AgentConfigType.KNOWLEDGE_BASE)
                .map(AgentConfig::getConfigId)
                .toList();
    }

    private ModelStrategy resolveModelStrategy(List<AgentConfig> configs) {
        String id = findConfigId(configs, AgentConfigCategory.STRATEGY, AgentConfigType.MODEL_STRATEGY);
        if (id == null) return null;
        return modelStrategyRepository.findById(id).orElse(null);
    }

    private ToolStrategy resolveToolStrategy(List<AgentConfig> configs) {
        String id = findConfigId(configs, AgentConfigCategory.STRATEGY, AgentConfigType.TOOL_STRATEGY);
        if (id == null) return null;
        return toolStrategyRepository.findById(id).orElse(null);
    }

    private GuardrailStrategy resolveGuardrailStrategy(List<AgentConfig> configs) {
        String id = findConfigId(configs, AgentConfigCategory.STRATEGY, AgentConfigType.GUARDRAIL_STRATEGY);
        if (id == null) return null;
        return guardrailStrategyRepository.findById(id).orElse(null);
    }

    private RetrievalStrategy resolveRetrievalStrategy(List<AgentConfig> configs) {
        String id = findConfigId(configs, AgentConfigCategory.STRATEGY, AgentConfigType.RETRIEVAL_STRATEGY);
        if (id == null) return null;
        return retrievalStrategyRepository.findById(id).orElse(null);
    }

    private String findConfigId(List<AgentConfig> configs, AgentConfigCategory category, AgentConfigType type) {
        return configs.stream()
                .sorted(Comparator.comparingInt(AgentConfig::getPriority).reversed())
                .filter(c -> c.getCategory() == category && c.getType() == type)
                .findFirst()
                .map(AgentConfig::getConfigId)
                .orElse(null);
    }


    private List<Object> resolveToolCallbacks(List<AgentToolInfo> agentToolInfos,
                                               com.agenthub.domain.model.strategy.ToolStrategy strategy) {
        List<Object> tools = new CopyOnWriteArrayList<>();
        var collect = agentToolInfos.stream().collect(Collectors.groupingBy(AgentToolInfo::getType));
        parallelStreamWithTtl(4, collect.entrySet(), entry -> {
            if (!entry.getValue().isEmpty()) {
                var toolCallbacks = resolveToolCallbacks(entry.getKey(), entry.getValue());
                if (!toolCallbacks.isEmpty()) tools.addAll(toolCallbacks);
            }
            return null;
        });
        return applyToolStrategy(tools, strategy);
    }

    private List<Object> applyToolStrategy(List<Object> tools,
                                            com.agenthub.domain.model.strategy.ToolStrategy strategy) {
        if (strategy == null || tools.isEmpty()) return tools;
        Set<org.springframework.ai.tool.ToolCallback> callbacks = tools.stream()
                .filter(org.springframework.ai.tool.ToolCallback.class::isInstance)
                .map(org.springframework.ai.tool.ToolCallback.class::cast)
                .collect(java.util.stream.Collectors.toSet());
        Set<org.springframework.ai.tool.ToolCallback> filtered = toolFilterUseCase.filterByStrategy(callbacks, strategy);
        return new java.util.ArrayList<>(filtered);
    }


    private Set<ToolCallback> resolveToolCallbacks(AgentToolType toolInfo, List<AgentToolInfo> toolIds) {
        return switch (toolInfo) {
            case SYSTEM_TOOL -> resolveSystemTools(toolIds);
            case MCP_TOOL -> resolveMcpTools(toolIds);
            default -> Set.of();
        };
    }

    /**
     * 注意：SystemTools 是类级别的启用停用控制，所以这里需要根据 class name 进行过滤
     *
     * @return
     */
    private Set<ToolCallback> resolveSystemTools(List<AgentToolInfo> toolIds) {
        return castCallbacks(toolCallbackResolver.resolveToolCallbacks(SYSTEM_TOOL, toolIds));
    }

    private Set<ToolCallback> resolveMcpTools(List<AgentToolInfo> toolIds) {
        return castCallbacks(toolCallbackResolver.resolveToolCallbacks(MCP_TOOL, toolIds));
    }

    private Set<ToolCallback> castCallbacks(Set<Object> callbacks) {
        return callbacks.stream()
                .filter(ToolCallback.class::isInstance)
                .map(ToolCallback.class::cast)
                .collect(Collectors.toSet());
    }
}
