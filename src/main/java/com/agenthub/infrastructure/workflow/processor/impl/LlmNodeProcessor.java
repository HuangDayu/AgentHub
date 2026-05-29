package com.agenthub.infrastructure.workflow.processor.impl;

import com.agenthub.application.command.AgentChatCommand;
import com.agenthub.application.port.out.agent.AgentChatPort;
import com.agenthub.application.port.out.repositories.SessionRepository;
import com.agenthub.domain.enums.workflow.NodeType;
import com.agenthub.domain.model.agent.Session;
import com.agenthub.domain.model.workflow.WorkflowChat;
import com.agenthub.domain.model.workflow.WorkflowContext;
import com.agenthub.domain.model.workflow.WorkflowNode;
import com.agenthub.infrastructure.workflow.processor.AbstractNodeProcessor;
import com.agenthub.infrastructure.workflow.variable.VariableResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LLM节点处理器。
 * 调用AgentChatPort进行LLM对话，支持流式响应。
 *
 * @author huangdayu
 */
@Component
@RequiredArgsConstructor
public class LlmNodeProcessor extends AbstractNodeProcessor {

    private final AgentChatPort agentChatPort;
    private final VariableResolver variableResolver;
    private final SessionRepository sessionRepository;

    @Override
    public String getSupportedType() {
        return NodeType.LLM.name();
    }

    @Override
    protected Mono<Map<String, Object>> doProcess(WorkflowNode node, WorkflowContext context) {
        return buildChatRequest(node, context)
                .flatMap(request -> executeChat(request, node));
    }

    private Mono<WorkflowChat> buildChatRequest(WorkflowNode node, WorkflowContext context) {
        return Mono.fromSupplier(() -> parseChatConfig(node, context));
    }

    private WorkflowChat parseChatConfig(WorkflowNode node, WorkflowContext context) {
        Map<String, Object> config = node.getConfig().getParameters();
        String agentId = (String) config.getOrDefault("agentId", "default");
        String userMessage = resolveMessage(config, context);
        String sessionId = context.getExecutionId();
        return new WorkflowChat(agentId, sessionId, userMessage);
    }

    private String resolveMessage(Map<String, Object> config, WorkflowContext context) {
        String promptTemplate = (String) config.getOrDefault("prompt", "");
        return variableResolver.resolveTemplateString(promptTemplate, context);
    }

    private Mono<Map<String, Object>> executeChat(WorkflowChat request, WorkflowNode node) {
        Boolean streaming = (Boolean) node.getConfig().getParameters().getOrDefault("streaming", false);

        if (Boolean.TRUE.equals(streaming)) {
            return executeStreamingChat(request);
        } else {
            return executeSyncChat(request);
        }
    }

    private Mono<Map<String, Object>> executeSyncChat(WorkflowChat workflowChat) {
        return Mono.fromCallable(() -> {
            // 确保 session 存在，如果不存在则创建
            ensureSessionExists(workflowChat.getAgentId(), workflowChat.getSessionId());

            var response = agentChatPort.chatMessages(new AgentChatCommand(workflowChat.getAgentId(), workflowChat.getSessionId(), workflowChat.getMessage(), List.of()));
            Map<String, Object> result = new HashMap<>();
            result.put("content", response.getText());
            result.put("success", true);
            return result;
        });
    }

    private Mono<Map<String, Object>> executeStreamingChat(WorkflowChat workflowChat) {
        // 确保 session 存在，如果不存在则创建
        ensureSessionExists(workflowChat.getAgentId(), workflowChat.getSessionId());

        return agentChatPort.streamMessages(new AgentChatCommand(workflowChat.getAgentId(), workflowChat.getSessionId(), workflowChat.getMessage(), List.of()))
                .collectList()
                .map(messages -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("messages", messages);
                    result.put("count", messages.size());
                    result.put("success", true);
                    return result;
                });
    }

    /**
     * 确保 Session 存在。如果不存在则创建。
     * 工作流执行时使用 executionId 作为 sessionId,但这个 session 可能还没创建。
     */
    private void ensureSessionExists(String agentId, String sessionId) {
        try {
            // 尝试检查 session 是否存在
            sessionRepository.existSession(sessionId, agentId);
        } catch (Exception e) {
            // 如果 session 不存在，创建一个
            if (e.getMessage() != null && e.getMessage().contains("Session not owned by agent")) {
                Session session = new Session();
                session.setId(sessionId);
                session.setAgentId(agentId);
                session.setName("Workflow Execution Session");
                session.setCreatedAt(Instant.now());
                sessionRepository.save(session);
            }
        }
    }
}
