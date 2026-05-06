package com.agenthub.infrastructure.agents;

import com.agenthub.application.port.out.repositories.*;
import com.agenthub.domain.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
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
    private final FunctionToolsRepository functionToolsRepository;
    private final SkillRepository skillRepository;
    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final PromptTemplateRepository promptTemplateRepository;
    private final ModelStrategyRepository modelStrategyRepository;
    private final RetrievalStrategyRepository retrievalStrategyRepository;
    private final ToolStrategyRepository toolStrategyRepository;
    private final GuardrailStrategyRepository guardrailStrategyRepository;

    public AbstractReActAgent getAgent(java.lang.String agentId) {
        return AGENT_POOL.computeIfAbsent(agentId, id -> agentFactory.create(createContext(agentId)));
    }

    private ReActAgentContext createContext(java.lang.String agentId) {
        Agent agent = agentRepository.findById(agentId).orElse(null);
        if (agent == null) return null;

        List<AgentConfig> configs = agentConfigRepository.findByAgentIdAndEnabled(agentId);
        return buildContext(agentId, agent, configs);
    }

    private ReActAgentContext buildContext(java.lang.String agentId, Agent agent, List<AgentConfig> configs) {
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
                .agentConfigs(configs)
                .build();
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
        List<AgentToolInfo> tools = new java.util.ArrayList<>();
        tools.addAll(resolveMcpTools(configs));
        tools.addAll(resolveHttpTools(configs));
        tools.addAll(resolveFunctionTools(configs));
        tools.addAll(resolveSkillTools(configs));
        return tools;
    }

    private List<AgentToolInfo> resolveMcpTools(List<AgentConfig> configs) {
        return configs.stream()
                .filter(c -> c.type() == AgentConfig.Type.MCP_TOOL)
                .map(this::toMcpToolInfo)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private AgentToolInfo toMcpToolInfo(AgentConfig config) {
        return mcpToolRepository.findById(config.configId())
                .map(tool -> buildToolInfo(AgentToolType.MCP_TOOLS, tool.id(), tool.name(), tool.description()))
                .orElse(null);
    }

    private List<AgentToolInfo> resolveHttpTools(List<AgentConfig> configs) {
        return configs.stream()
                .filter(c -> c.category() == AgentConfig.Category.TOOL && c.type() != AgentConfig.Type.MCP_TOOL)
                .map(this::toHttpToolInfo)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private AgentToolInfo toHttpToolInfo(AgentConfig config) {
        return httpToolRepository.findById(new String(config.configId()))
                .map(tool -> buildToolInfo(AgentToolType.HTTP_TOOLS, tool.id(), tool.name(), tool.description()))
                .orElse(null);
    }

    private List<AgentToolInfo> resolveFunctionTools(List<AgentConfig> configs) {
        return configs.stream()
                .filter(c -> c.type() == AgentConfig.Type.FUNCTION_TOOL)
                .map(this::toFunctionToolInfo)
                .filter(java.util.Objects::nonNull)
                .toList();
    }


    private AgentToolInfo toFunctionToolInfo(AgentConfig config) {
        return functionToolsRepository.findById(config.configId())
                .map(tool -> buildToolInfo(AgentToolType.FUNCTION_TOOLS, tool.getId(), tool.getToolClassName(), tool.getDescription()))
                .orElse(null);
    }

    private List<AgentToolInfo> resolveSkillTools(List<AgentConfig> configs) {
        return configs.stream()
                .filter(c -> c.type() == AgentConfig.Type.SKILL_TOOL)
                .map(this::toSkillToolInfo)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private AgentToolInfo toSkillToolInfo(AgentConfig config) {
        return skillRepository.findById(config.configId())
                .map(skill -> buildToolInfo(AgentToolType.SKILL_TOOLS, skill.getId(), skill.getName(), skill.getDescription()))
                .orElse(null);
    }

    private AgentToolInfo buildToolInfo(AgentToolType type, java.lang.String id, java.lang.String name, java.lang.String description) {
        return AgentToolInfo.builder()
                .type(type)
                .id(id)
                .name(name)
                .description(description)
                .enabled(true)
                .build();
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
