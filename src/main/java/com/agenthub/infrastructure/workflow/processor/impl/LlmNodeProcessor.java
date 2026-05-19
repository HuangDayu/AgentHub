package com.agenthub.infrastructure.workflow.processor.impl;

import com.agenthub.domain.model.workflow.WorkflowChat;
import com.agenthub.application.port.out.agent.AgentChatPort;
import com.agenthub.domain.enums.workflow.NodeType;
import com.agenthub.domain.model.workflow.WorkflowContext;
import com.agenthub.domain.model.workflow.WorkflowNode;
import com.agenthub.infrastructure.workflow.processor.AbstractNodeProcessor;
import com.agenthub.infrastructure.workflow.variable.VariableResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
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
            var response = agentChatPort.call(workflowChat.getAgentId(), workflowChat.getSessionId(), workflowChat.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("content", response.getText());
            result.put("success", true);
            return result;
        });
    }

    private Mono<Map<String, Object>> executeStreamingChat(WorkflowChat workflowChat) {
        return agentChatPort.streamMessages(workflowChat.getAgentId(), workflowChat.getSessionId(), workflowChat.getMessage())
            .collectList()
            .map(messages -> {
                Map<String, Object> result = new HashMap<>();
                result.put("messages", messages);
                result.put("count", messages.size());
                result.put("success", true);
                return result;
            });
    }
}
