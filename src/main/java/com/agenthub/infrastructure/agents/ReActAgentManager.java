package com.agenthub.infrastructure.agents;

import com.agenthub.application.port.out.repositories.*;
import com.agenthub.common.exception.NotFoundException;
import com.agenthub.domain.model.*;
import com.agenthub.infrastructure.tools.system_tools.SystemToolsFactory;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author huangdayu
 */
@Component
@RequiredArgsConstructor
public class ReActAgentManager {

    public static final Map<java.lang.String, AbstractReActAgent> AGENT_POOL = new ConcurrentHashMap<>();
    private final ReActAgentFactory agentFactory;
    private final AgentRepository agentRepository;
    private final AgentConfigRepository agentConfigRepository;
    private final McpToolRepository mcpToolRepository;
    private final HttpToolRepository httpToolRepository;
    private final SystemToolsRepository systemToolsRepository;
    private final SkillRepository skillRepository;
    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final PromptTemplateRepository promptTemplateRepository;
    private final ModelStrategyRepository modelStrategyRepository;
    private final RetrievalStrategyRepository retrievalStrategyRepository;
    private final ToolStrategyRepository toolStrategyRepository;
    private final GuardrailStrategyRepository guardrailStrategyRepository;
    private final WorkspaceRepository workspaceRepository;
    private final SystemToolsFactory systemToolsFactory;

    public AbstractReActAgent getAgent(java.lang.String agentId) {
        return AGENT_POOL.computeIfAbsent(agentId, id -> agentFactory.create(createContext(agentId)));
    }

    private ReActAgentContext createContext(java.lang.String agentId) {
        Agent agent = agentRepository.findById(agentId).orElse(null);
        if (agent == null) return null;

        List<AgentConfig> configs = agentConfigRepository.findByAgentIdAndEnabled(agentId);
        return buildContext(agentId, agent, configs, agent.getWorkspaceId());
    }

    private ReActAgentContext buildContext(java.lang.String agentId, Agent agent, List<AgentConfig> configs, String workspaceId) {
        return ReActAgentContext.builder()
                .agentId(agentId)
                .agentName(agent.getName())
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
        Optional<Workspace> optional = workspaceRepository.findById(workspaceId);
        if (optional.isEmpty()) {
            throw new NotFoundException("Workspace not found: " + workspaceId);
        }
        Workspace workspace = optional.get();
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
        Path path = Paths.get(System.getProperty("user.home"), ".agenthub", workspace.workspaceCode(), subPath);
        Files.createDirectories(path);
        return path;
    }


    private java.lang.String resolveChatModelId(List<AgentConfig> configs) {
        return findConfigId(configs, AgentConfig.Category.MODEL, AgentConfig.Type.CHAT_MODEL);
    }

    private java.lang.String resolveSystemPrompt(List<AgentConfig> configs) {
        java.lang.String promptId = findConfigId(configs, AgentConfig.Category.PROMPT, AgentConfig.Type.SYSTEM_PROMPT);
        if (promptId == null) return null;
        return promptTemplateRepository.findById(promptId).map(PromptTemplateInfo::content).orElse(null);
    }

    private List<AgentToolInfo> resolveTools(List<AgentConfig> configs) {
        return configs.parallelStream().filter(c -> c.category() == AgentConfig.Category.TOOL)
                .map(v -> new AgentToolInfo(AgentToolType.valueOf(v.type().name()), v.configId(), v.name(), v.description(), v.enabled()))
                .toList();
    }


    private List<java.lang.String> resolveToolIds(List<AgentConfig> configs) {
        return configs.stream()
                .filter(c -> c.category() == AgentConfig.Category.TOOL)
                .map(AgentConfig::configId)
                .toList();
    }

    private List<java.lang.String> resolveKnowledgeIds(List<AgentConfig> configs) {
        return configs.stream()
                .filter(c -> c.type() == AgentConfig.Type.KNOWLEDGE_BASE)
                .map(AgentConfig::configId)
                .toList();
    }

    private ModelStrategy resolveModelStrategy(List<AgentConfig> configs) {
        java.lang.String id = findConfigId(configs, AgentConfig.Category.STRATEGY, AgentConfig.Type.MODEL_STRATEGY);
        if (id == null) return null;
        return modelStrategyRepository.findById(id).orElse(null);
    }

    private ToolStrategy resolveToolStrategy(List<AgentConfig> configs) {
        java.lang.String id = findConfigId(configs, AgentConfig.Category.STRATEGY, AgentConfig.Type.TOOL_STRATEGY);
        if (id == null) return null;
        return toolStrategyRepository.findById(id).orElse(null);
    }

    private GuardrailStrategy resolveGuardrailStrategy(List<AgentConfig> configs) {
        java.lang.String id = findConfigId(configs, AgentConfig.Category.STRATEGY, AgentConfig.Type.GUARDRAIL_STRATEGY);
        if (id == null) return null;
        return guardrailStrategyRepository.findById(id).orElse(null);
    }

    private RetrievalStrategy resolveRetrievalStrategy(List<AgentConfig> configs) {
        java.lang.String id = findConfigId(configs, AgentConfig.Category.STRATEGY, AgentConfig.Type.RETRIEVAL_STRATEGY);
        if (id == null) return null;
        return retrievalStrategyRepository.findById(id).orElse(null);
    }

    private java.lang.String findConfigId(List<AgentConfig> configs, AgentConfig.Category category, AgentConfig.Type type) {
        return configs.stream()
                .filter(c -> c.category() == category && c.type() == type)
                .findFirst()
                .map(AgentConfig::configId)
                .orElse(null);
    }
}
