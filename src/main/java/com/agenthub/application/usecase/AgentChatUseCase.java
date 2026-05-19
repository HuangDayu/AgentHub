package com.agenthub.application.usecase;

import com.agenthub.application.port.out.agent.AgentChatPort;
import com.agenthub.application.port.out.repositories.AgentRepository;
import com.agenthub.application.port.out.repositories.SessionRepository;
import com.agenthub.domain.exception.NotFoundException;
import com.agenthub.domain.model.agent.AbstractReActAgent;
import com.agenthub.domain.model.agent.AgentMessage;
import com.agenthub.domain.model.agent.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.LinkedList;
import java.util.List;

import static com.agenthub.domain.model.agent.ChatMessage.*;
import static org.springframework.ai.util.json.JsonParser.toJson;

/**
 * Agent对话用例，处理Agent对话和Session管理。
 *
 * @author huangdayu
 */
@Component
@RequiredArgsConstructor
public class AgentChatUseCase implements AgentChatPort {

    private final AgentRepository agentRepository;
    private final SessionRepository sessionRepository;
    private final AgentPoolUseCase agentPoolUseCase;


    @Override
    public AgentMessage chatMessages(String agentId, String sessionId, String userMessage) {
        sessionRepository.existSession(sessionId, agentId);
        AbstractReActAgent agent = getAgent(agentId, sessionId);
        if (agent == null) throw new NotFoundException("Agent create failed :" + agentId);
        List<ChatMessage> messages = new LinkedList<>();
        messages.add(user(sessionId, userMessage));
        AgentMessage response = agent.call(sessionId, userMessage);
        messages.add(assistant(sessionId, response.getText()));
        sessionRepository.saveMessages(messages);
        return response;
    }

    @Override
    public Flux<AgentMessage> streamMessages(String agentId, String sessionId, String userMessage) {
        sessionRepository.existSession(sessionId, agentId);
        AbstractReActAgent agent = getAgent(agentId, sessionId);
        if (agent == null) throw new NotFoundException("Agent create failed :" + agentId);
        StringBuilder responseBuilder = new StringBuilder();
        List<ChatMessage> messages = new LinkedList<>();
        messages.add(user(sessionId, userMessage));
        return agent.streamMessages(sessionId, userMessage)
                .doOnNext(msg -> appendMessages(sessionId, messages, responseBuilder, msg))
                .onErrorResume(throwable -> {
                    String errorMessage = "对话过程中发生错误: " + throwable.getMessage();
                    messages.add(system(sessionId, errorMessage));
                    return Flux.just(new AgentMessage(AgentMessage.MessageType.SYSTEM, errorMessage));
                })
                .doFinally(signal -> saveMessages(messages));
    }

    @Override
    public void interrupt(String agentId, String sessionId) {
        AbstractReActAgent agent = getAgent(agentId, sessionId);
        if (agent != null) agent.interrupt();
    }

    private AbstractReActAgent getAgent(String agentId, String sessionId) {
        return agentPoolUseCase.getAgent(agentId, sessionId);
    }

    /**
     * 追加响应内容。
     */
    private void appendMessages(String sessionId, List<ChatMessage> messages, StringBuilder builder, AgentMessage message) {
        switch (message.getMessageType()) {
            case ASSISTANT -> handleAssistantMessage(sessionId, messages, builder, message);
            case TOOL -> messages.add(tool(sessionId, toJson(message.getToolResponses())));
            case SYSTEM -> messages.add(system(sessionId, message.getText()));
            default -> {
            }
        }
    }

    private void handleAssistantMessage(String sessionId, List<ChatMessage> messages, StringBuilder builder, AgentMessage msg) {
        if (msg.getText() != null) {
            builder.append(msg.getText());
        }
        if (msg.getToolCalls() != null && !msg.getToolCalls().isEmpty()) {
            flushPendingText(sessionId, messages, builder);
            messages.add(assistant(sessionId, toJson(msg.getToolCalls())));
        }
        if ("STOP".equalsIgnoreCase((String) msg.getMetadata().getOrDefault("finishReason", ""))) {
            flushPendingText(sessionId, messages, builder);
        }
    }

    private void flushPendingText(String sessionId, List<ChatMessage> messages, StringBuilder builder) {
        String text = builder.toString().strip().replaceFirst("^\n", "").replaceFirst("\n$", "");
        if (!text.isEmpty()) messages.add(assistant(sessionId, text));
        builder.delete(0, builder.length());
    }

    /**
     * 保存助手消息。
     */
    private void saveMessages(List<ChatMessage> messages) {
        sessionRepository.saveMessages(messages);
    }

    /**
     * 验证Agent存在。
     */
    private void validateAgentExists(String agentId) {
        agentRepository.findById(agentId)
                .orElseThrow(() -> new NotFoundException("Agent not found: " + agentId));
    }


}
