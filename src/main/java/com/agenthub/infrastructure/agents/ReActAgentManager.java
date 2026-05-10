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

    public static final Map<String, AbstractReActAgent> AGENT_POOL = new ConcurrentHashMap<>();
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

    public AbstractReActAgent getAgent(String agentId) {
        return AGENT_POOL.computeIfAbsent(agentId, id -> agentFactory.create(createContext(agentId)));
    }

    private ReActAgentContext createContext(String agentId) {
        Agent agent = agentRepository.findById(agentId).orElse(null);
        if (agent == null) return null;

        List<AgentConfig> configs = agentConfigRepository.findByAgentIdAndEnabled(agentId);
        return buildContext(agentId, agent, configs, agent.getWorkspaceId());
    }

    private ReActAgentContext buildContext(String agentId, Agent agent, List<AgentConfig> configs, String workspaceId) {
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
