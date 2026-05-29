package com.agenthub.infrastructure.tools.system_tools.base_tools;

import com.agenthub.application.port.out.repositories.SubagentRepository;
import com.agenthub.application.port.out.repositories.SubsessionRepository;
import com.agenthub.domain.model.agent.*;
import com.agenthub.infrastructure.agents.subagent.SubagentEngine;
import com.agenthub.infrastructure.tools.system_tools.annotations.AgentTools;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.agenthub.common.utils.RandomUtils.randomId;
import static com.agenthub.infrastructure.tools.system_tools.SystemToolsUtils.getAgentContext;

/**
 * 子智能体管理工具，供Agent在运行时自主创建、控制和查询Subagent。
 */
@Slf4j
@RequiredArgsConstructor
@AgentTools(name = "SubagentManageTools",
        description = "子智能体工具，提供子Agent的创建、控制、调度和查询功能")
public class SubagentManageTools {

    private final SubagentRepository subagentRepository;
    private final SubsessionRepository subsessionRepository;
    private final SubagentEngine subagentEngine;


    @Tool(description = "创建子Agent并异步执行任务。返回子Agent ID。")
    @Transactional
    public Flux<AgentMessage> createSubagent(
            @ToolParam(description = "子Agent名称") String name,
            @ToolParam(description = "系统提示词") String systemPrompt,
            @ToolParam(description = "要执行的任务") String task,
            @ToolParam(required = false, description = "工具列表，逗号分隔") String tools,
            @ToolParam(required = false, description = "知识库ID列表") String knowledgeIds,
            @ToolParam(required = false, description = "模型配置ID") String modelConfigId,
            ToolContext toolContext) {
        ReActAgentContext parent = getAgentContext(toolContext);
        Subagent subagent = createSubagentRecord(parent, parent.getAgent(), name, systemPrompt, modelConfigId);
        Subsession subsession = createSubsession(parent.getSessionId(), subagent);
        ReActAgentContext ctx = buildSubagentContext(parent, subagent, subsession, tools, knowledgeIds);
        return subagentEngine.executeSubagent(subagent, subsession, ctx, task);
    }

    @Tool(description = "获取当前会话的所有子Agent列表")
    public String listSubagents(ToolContext toolContext) {
        ReActAgentContext context = getAgentContext(toolContext);
        return subsessionRepository.findByParentSessionId(context.getSessionId()).stream()
                .map(ss -> subagentRepository.findById(ss.getSubagentId()).orElse(null))
                .filter(sa -> sa != null)
                .map(sa -> fmtSubagent(sa))
                .collect(Collectors.joining("\n"));
    }

    @Tool(description = "获取指定子Agent的运行状态")
    public String getSubagentStatus(
            @ToolParam(description = "子Agent ID") String subagentId,
            ToolContext toolContext) {
        Subagent sa = subagentRepository.findById(subagentId).orElse(null);
        if (sa == null) return "子Agent不存在: " + subagentId;
        return fmtStatus(sa);
    }

    @Tool(description = "停止一个正在运行的子Agent")
    public String stopSubagent(
            @ToolParam(description = "子Agent ID") String subagentId,
            @ToolParam(description = "子Agent 的subsessionId") String subsessionId,
            ToolContext toolContext) {
        Subagent subagent = subagentRepository.findById(subagentId).orElse(null);
        if (subagent == null) return "子Agent不存在: " + subagentId;
        subagentEngine.stop(subagent, subsessionId);
        subagent.setStatus("INTERRUPTED");
        subagentRepository.save(subagent);
        return "子Agent " + subagent.getName() + " 已停止";
    }

    @Tool(description = "获取子Agent的对话记录")
    public String getSubagentMessages(
            @ToolParam(description = "子Agent ID") String subagentId,
            ToolContext toolContext) {
        Subsession ss = subsessionRepository.findBySubagentId(subagentId).stream()
                .findFirst().orElse(null);
        if (ss == null) return "子Agent的会话不存在";
        return subsessionRepository.findByIdWithMessages(ss.getId())
                .map(s -> s.getMessages().stream()
                        .map(m -> String.format("[%s] %s", m.getRole(), m.getContent()))
                        .collect(Collectors.joining("\n")))
                .orElse("暂无对话记录");
    }


    private Subagent createSubagentRecord(ReActAgentContext pc, Agent parent,
                                          String name, String prompt, String modelId) {
        Subagent sa = new Subagent();
        sa.setId(randomId());
        sa.setParentAgentId(parent.getId());
        sa.setName(name);
        sa.setSystemPrompt(prompt);
        sa.setModelConfigId(modelId != null ? modelId : pc.getChatModelId());
        sa.setTenantId(parent.getTenantId());
        sa.setWorkspaceId(parent.getWorkspaceId());
        sa.setStatus("RUNNING");
        sa.setCreatedAt(Instant.now());
        sa.setUpdatedAt(Instant.now());
        return subagentRepository.save(sa);
    }

    private Subsession createSubsession(String parentSessionId, Subagent subagent) {
        Subsession ss = Subsession.create(parentSessionId, subagent.getId(), subagent.getName());
        return subsessionRepository.save(ss);
    }

    private ReActAgentContext buildSubagentContext(ReActAgentContext pc,
                                                   Subagent sa, Subsession ss,
                                                   String tools, String kbIds) {
        return ReActAgentContext.builder()
                .agent(mapToAgent(sa))
                .sessionId(ss.getId())
                .chatModelId(sa.getModelConfigId())
                .systemPrompt(sa.getSystemPrompt())
                .tools(parseToolConfig(pc, tools))
                .knowledgeIds(parseIdList(kbIds))
                .workspace(pc.getWorkspace())
                .build();
    }

    private com.agenthub.domain.model.agent.Agent mapToAgent(Subagent sa) {
        com.agenthub.domain.model.agent.Agent agent = new com.agenthub.domain.model.agent.Agent();
        agent.setId(sa.getId());
        agent.setName(sa.getName());
        agent.setDescription(sa.getDescription());
        agent.setTenantId(sa.getTenantId());
        agent.setWorkspaceId(sa.getWorkspaceId());
        return agent;
    }

    private List<AgentToolInfo> parseToolConfig(ReActAgentContext pc, String tools) {
        if (tools == null || tools.isBlank()) return List.of();
        List<AgentToolInfo> result = new ArrayList<>();
        for (String t : tools.split(",")) {
            pc.getTools().stream()
                    .filter(ti -> ti.getName().equals(t.strip()))
                    .findFirst().ifPresent(result::add);
        }
        return result;
    }

    private List<String> parseIdList(String ids) {
        if (ids == null || ids.isBlank()) return List.of();
        return List.of(ids.split(",")).stream()
                .map(String::strip).filter(s -> !s.isEmpty()).toList();
    }

    private String fmtSubagent(Subagent sa) {
        return String.format("{id:%s, name:%s, status:%s, createdAt:%s}",
                sa.getId(), sa.getName(), sa.getStatus(), sa.getCreatedAt());
    }

    private String fmtStatus(Subagent sa) {
        return String.format("名称: %s\n状态: %s\n描述: %s\n创建时间: %s",
                sa.getName(), sa.getStatus(), sa.getDescription(), sa.getCreatedAt());
    }
}
