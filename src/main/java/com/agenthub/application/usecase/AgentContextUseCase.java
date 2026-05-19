package com.agenthub.application.usecase;

import com.agenthub.application.port.out.repositories.*;
import com.agenthub.domain.enums.AgentConfigCategory;
import com.agenthub.domain.enums.AgentConfigType;
import com.agenthub.domain.enums.AgentToolType;
import com.agenthub.domain.exception.NotFoundException;
import com.agenthub.domain.model.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author huangdayu
 */
@Component
@RequiredArgsConstructor
public class AgentContextUseCase {
    private final AgentRepository agentRepository;
    private final AgentConfigRepository agentConfigRepository;
    private final PromptTemplateRepository promptTemplateRepository;
    private final ModelStrategyRepository modelStrategyRepository;
    private final RetrievalStrategyRepository retrievalStrategyRepository;
    private final ToolStrategyRepository toolStrategyRepository;
    private final GuardrailStrategyRepository guardrailStrategyRepository;
    private final WorkspaceRepository workspaceRepository;

    public ReActAgentContext buildContext(String agentId, String sessionId) {
        Agent agent = agentRepository.findById(agentId).orElseThrow(() -> new NotFoundException("Agent not found: " + agentId));
        List<AgentConfig> configs = agentConfigRepository.findByAgentIdAndEnabled(agentId);
        return buildContext(agent, sessionId, configs, agent.getWorkspaceId());
    }


    private ReActAgentContext buildContext(Agent agent, String sessionId, List<AgentConfig> configs, String workspaceId) {
        return ReActAgentContext.builder()
                .agent(agent)
                .sessionId(sessionId)
                .chatModelId(resolveChatModelId(configs))
                .systemPrompt(resolveSystemPrompt(configs))
                .tools(resolveTools(configs))
                .knowledgeIds(resolveKnowledgeIds(configs))
                .modelStrategy(resolveModelStrategy(configs))
                .toolStrategy(resolveToolStrategy(configs))
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
        if (promptId == null) return null;
        return promptTemplateRepository.findById(promptId).map(PromptTemplateInfo::getContent).orElse(null);
    }

    private List<AgentToolInfo> resolveTools(List<AgentConfig> configs) {
        return configs.parallelStream().filter(c -> c.getCategory() == AgentConfigCategory.TOOL)
                .map(v -> new AgentToolInfo(AgentToolType.valueOf(v.getType().name()), v.getConfigId(), v.getName(), v.getDescription(), v.isEnabled()))
                .toList();
    }


    private List<java.lang.String> resolveToolIds(List<AgentConfig> configs) {
        return configs.stream()
                .filter(c -> c.getCategory() == AgentConfigCategory.TOOL)
                .map(AgentConfig::getConfigId)
                .toList();
    }

    private List<java.lang.String> resolveKnowledgeIds(List<AgentConfig> configs) {
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
                .filter(c -> c.getCategory() == category && c.getType() == type)
                .findFirst()
                .map(AgentConfig::getConfigId)
                .orElse(null);
    }
}
