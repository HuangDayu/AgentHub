package com.agenthub.application.usecase;

import com.agenthub.application.command.RunSubagentCommand;
import com.agenthub.application.command.SubagentExecutionCommand;
import com.agenthub.application.dto.SubagentMessageOutput;
import com.agenthub.application.dto.SubagentRunOutput;
import com.agenthub.application.dto.SubagentRuntimeOutput;
import com.agenthub.application.port.out.agent.SubagentExecutionPort;
import com.agenthub.application.port.out.repositories.SubagentRepository;
import com.agenthub.application.port.out.repositories.SubsessionRepository;
import com.agenthub.domain.enums.AgentToolType;
import com.agenthub.domain.model.agent.*;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.agenthub.common.utils.RandomUtils.randomId;

/**
 * 子Agent运行时用例。
 */
@Component
@RequiredArgsConstructor
public class SubagentRuntimeUseCase {

    private final SubagentRepository subagentRepository;
    private final SubsessionRepository subsessionRepository;
    private final SubagentExecutionPort subagentExecutionPort;

    /**
     * 创建子Agent并后台执行任务。
     */
    @Transactional
    public SubagentRunOutput run(RunSubagentCommand command) {
        Subagent subagent = createSubagent(command);
        Subsession subsession = createSubsession(command, subagent);
        subagentExecutionPort.execute(executionCommand(command, subagent, subsession));
        return runOutput(subagent, subsession, null);
    }

    /**
     * 列出当前父会话的子Agent。
     */
    public List<SubagentRuntimeOutput> list(ReActAgentContext context) {
        return subsessionRepository.findByParentSessionId(context.getSessionId()).stream()
                .map(this::runtimeOutput).toList();
    }

    /**
     * 查询子Agent状态。
     */
    public SubagentRuntimeOutput status(String subagentId, String subsessionId) {
        Subagent subagent = findSubagent(subagentId);
        if (subagent == null) return runtimeMessage(subagentId, subsessionId, "子Agent不存在");
        if (!subsessionExists(subagentId, subsessionId))
            return runtimeMessage(subagentId, subsessionId, "子Agent的会话不存在");
        return runtimeOutput(subagent, subsessionId);
    }

    /**
     * 停止子Agent。
     */
    public SubagentRuntimeOutput stop(String subagentId, String subsessionId) {
        Subagent subagent = findSubagent(subagentId);
        if (subagent == null) return runtimeMessage(subagentId, subsessionId, "子Agent不存在");
        if (!subsessionExists(subagentId, subsessionId))
            return runtimeMessage(subagentId, subsessionId, "子Agent的会话不存在");
        return stopSubagent(subagent, subsessionId);
    }

    /**
     * 获取子Agent消息。
     */
    public List<SubagentMessageOutput> messages(String subagentId, String subsessionId) {
        if (!subsessionExists(subagentId, subsessionId)) return List.of();
        return messageOutputs(subsessionId);
    }

    /**
     * 获取子Agent结果。
     */
    public SubagentRuntimeOutput result(String subagentId, String subsessionId) {
        Subagent subagent = findSubagent(subagentId);
        if (subagent == null) return runtimeMessage(subagentId, subsessionId, "子Agent不存在");
        if (!subsessionExists(subagentId, subsessionId))
            return runtimeMessage(subagentId, subsessionId, "子Agent的会话不存在");
        return resultOutput(subagent, subsessionId);
    }

    private SubagentExecutionCommand executionCommand(RunSubagentCommand command, Subagent subagent, Subsession subsession) {
        SubagentExecutionCommand execution = new SubagentExecutionCommand();
        execution.setSubagent(subagent);
        execution.setSubsession(subsession);
        execution.setContext(buildContext(command, subagent, subsession));
        execution.setInput(command.getTask());
        return execution;
    }

    private SubagentRunOutput runOutput(Subagent subagent, Subsession subsession, String result) {
        SubagentRunOutput output = new SubagentRunOutput();
        output.setSubagentId(subagent.getId());
        output.setSubsessionId(subsession.getId());
        output.setStatus(subagent.getStatus());
        output.setResult(result);
        return output;
    }

    private Subagent createSubagent(RunSubagentCommand command) {
        Subagent subagent = newSubagent(command);
        return subagentRepository.save(subagent);
    }

    private Subagent newSubagent(RunSubagentCommand command) {
        Subagent subagent = new Subagent();
        fillSubagent(command, subagent);
        return subagent;
    }

    private void fillSubagent(RunSubagentCommand command, Subagent subagent) {
        Agent parent = command.getParentContext().getAgent();
        subagent.setId(randomId());
        subagent.setParentAgentId(parent.getId());
        subagent.setName(command.getName());
        subagent.setSystemPrompt(command.getSystemPrompt());
        fillSubagentConfig(command, subagent, parent);
    }

    private void fillSubagentConfig(RunSubagentCommand command, Subagent subagent, Agent parent) {
        subagent.setModelConfigId(resolveModelId(command));
        subagent.setTenantId(parent.getTenantId());
        subagent.setWorkspaceId(parent.getWorkspaceId());
        subagent.setStatus("RUNNING");
        subagent.setCreatedAt(Instant.now());
        subagent.setUpdatedAt(Instant.now());
    }

    private String resolveModelId(RunSubagentCommand command) {
        String modelId = command.getModelConfigId();
        if (modelId != null) return modelId;
        return command.getParentContext().getChatModelId();
    }

    private Subsession createSubsession(RunSubagentCommand command, Subagent subagent) {
        String parentSessionId = command.getParentContext().getSessionId();
        Subsession subsession = Subsession.create(parentSessionId, subagent.getId(), subagent.getName());
        return subsessionRepository.save(subsession);
    }

    private ReActAgentContext buildContext(RunSubagentCommand command, Subagent subagent, Subsession subsession) {
        ReActAgentContext parent = command.getParentContext();
        return baseContext(parent, subagent, subsession)
                .toolInfos(parseTools(parent, command.getTools()))
                .toolCallbacks(parseToolCallbacks(parent, command.getTools()))
                .knowledgeIds(parseKnowledge(parent, command.getKnowledgeIds()))
                .build();
    }

    private List<Object> parseToolCallbacks(ReActAgentContext parent, List<String> tools) {
        return parent.getToolCallbacks().stream()
                .filter(v -> {
                    if (v instanceof ToolCallback toolCallback) {
                        return tools.contains(toolCallback.getToolDefinition().name());
                    }
                    return false;
                }).toList();
    }

    private ReActAgentContext.ReActAgentContextBuilder baseContext(ReActAgentContext parent, Subagent subagent, Subsession subsession) {
        return ReActAgentContext.builder().agent(mapToAgent(subagent))
                .sessionId(subsession.getId()).chatModelId(subagent.getModelConfigId())
                .systemPrompt(subagent.getSystemPrompt()).modelStrategy(parent.getModelStrategy())
                .toolStrategy(parent.getToolStrategy()).guardrailStrategy(parent.getGuardrailStrategy())
                .retrievalStrategy(parent.getRetrievalStrategy()).agentConfigs(parent.getAgentConfigs())
                .workspace(parent.getWorkspace());
    }

    private List<AgentToolInfo> parseTools(ReActAgentContext parent, List<String> tools) {
        List<AgentToolInfo> parentTools = executableTools(parent);
        if (tools == null || tools.isEmpty()) return parentTools;
        return filterTools(parentTools, tools);
    }

    private List<AgentToolInfo> executableTools(ReActAgentContext parent) {
        return defaultList(parent.getToolInfos()).stream()
                .filter(tool -> !isSubagentTool(tool))
                .toList();
    }

    private boolean isSubagentTool(AgentToolInfo tool) {
        return tool.getType() == AgentToolType.SYSTEM_TOOL
                && matchesTool(tool, "SubagentManageTools");
    }

    private List<AgentToolInfo> filterTools(List<AgentToolInfo> parentTools, List<String> tools) {
        List<AgentToolInfo> result = new ArrayList<>();
        for (String tool : tools) addTool(parentTools, result, tool.strip());
        return result;
    }

    private void addTool(List<AgentToolInfo> parentTools, List<AgentToolInfo> result, String tool) {
        parentTools.stream().filter(t -> matchesTool(t, tool))
                .findFirst().ifPresent(result::add);
    }

    private boolean matchesTool(AgentToolInfo info, String tool) {
        return tool.equals(info.getName()) || tool.equals(info.getConfigId());
    }

    private List<String> parseKnowledge(ReActAgentContext parent, String ids) {
        if (ids == null || ids.isBlank()) return defaultList(parent.getKnowledgeIds());
        return List.of(ids.split(",")).stream().map(String::strip)
                .filter(s -> !s.isEmpty()).toList();
    }

    private <T> List<T> defaultList(List<T> values) {
        return values == null ? List.of() : values;
    }

    private Subagent findSubagent(String subagentId) {
        return subagentRepository.findById(subagentId).orElse(null);
    }

    private boolean subsessionExists(String subagentId, String subsessionId) {
        return subsessionRepository.findById(subsessionId)
                .filter(ss -> subagentId.equals(ss.getSubagentId()))
                .isPresent();
    }

    private List<SubagentMessageOutput> messageOutputs(String subsessionId) {
        return subsessionRepository.findByIdWithMessages(subsessionId)
                .map(s -> messageOutputs(s.getMessages()))
                .orElse(List.of());
    }

    private List<SubagentMessageOutput> messageOutputs(List<ChatMessage> messages) {
        return messages.stream().map(this::messageOutput).toList();
    }

    private SubagentMessageOutput messageOutput(ChatMessage message) {
        SubagentMessageOutput output = new SubagentMessageOutput();
        output.setRole(message.getRole());
        output.setContent(message.getContent());
        output.setCreatedAt(message.getCreatedAt());
        return output;
    }

    private String latestAssistantMessage(String subsessionId) {
        return subsessionRepository.findByIdWithMessages(subsessionId)
                .map(s -> latestAssistantMessage(s.getMessages()))
                .orElse("暂无结果");
    }

    private String latestAssistantMessage(List<ChatMessage> messages) {
        return messages.reversed().stream().filter(this::isAssistantText)
                .findFirst().map(ChatMessage::getContent).orElse("暂无结果");
    }

    private boolean isAssistantText(ChatMessage message) {
        return "ASSISTANT".equals(message.getRole()) && !message.getContent().isBlank();
    }

    private SubagentRuntimeOutput stopSubagent(Subagent subagent, String subsessionId) {
        if (!subagentExecutionPort.stop(subagent, subsessionId)) {
            return runtimeMessage(subagent, subsessionId, "子Agent未在运行");
        }
        markInterrupted(subagent);
        return runtimeMessage(subagent, subsessionId, "子Agent已停止");
    }

    private SubagentRuntimeOutput resultOutput(Subagent subagent, String subsessionId) {
        SubagentRuntimeOutput output = runtimeOutput(subagent, subsessionId);
        output.setResult(latestAssistantMessage(subsessionId));
        return output;
    }

    private void markInterrupted(Subagent subagent) {
        subagent.setStatus("INTERRUPTED");
        subagentRepository.save(subagent);
    }

    private SubagentRuntimeOutput runtimeOutput(Subsession subsession) {
        return subagentRepository.findById(subsession.getSubagentId())
                .map(sa -> runtimeOutput(sa, subsession.getId())).orElse(runtimeMessage(subsession));
    }

    private SubagentRuntimeOutput runtimeOutput(Subagent subagent, String subsessionId) {
        SubagentRuntimeOutput output = new SubagentRuntimeOutput();
        fillRuntimeOutput(output, subagent, subsessionId);
        return output;
    }

    private void fillRuntimeOutput(SubagentRuntimeOutput output, Subagent subagent, String subsessionId) {
        output.setSubagentId(subagent.getId());
        output.setSubsessionId(subsessionId);
        output.setName(subagent.getName());
        output.setDescription(subagent.getDescription());
        output.setStatus(subagent.getStatus());
        output.setCreatedAt(subagent.getCreatedAt());
    }

    private SubagentRuntimeOutput runtimeMessage(Subsession subsession) {
        return runtimeMessage(subsession.getSubagentId(), subsession.getId(), "子Agent不存在");
    }

    private SubagentRuntimeOutput runtimeMessage(Subagent subagent, String subsessionId, String message) {
        SubagentRuntimeOutput output = runtimeOutput(subagent, subsessionId);
        output.setMessage(message);
        return output;
    }

    private SubagentRuntimeOutput runtimeMessage(String subagentId, String subsessionId, String message) {
        SubagentRuntimeOutput output = new SubagentRuntimeOutput();
        output.setSubagentId(subagentId);
        output.setSubsessionId(subsessionId);
        output.setMessage(message);
        return output;
    }

    private Agent mapToAgent(Subagent subagent) {
        Agent agent = new Agent();
        agent.setId(subagent.getId());
        agent.setName(subagent.getName());
        agent.setDescription(subagent.getDescription());
        agent.setTenantId(subagent.getTenantId());
        agent.setWorkspaceId(subagent.getWorkspaceId());
        return agent;
    }
}
