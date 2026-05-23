package com.agenthub.application.usecase;

import com.agenthub.application.port.out.agent.AgentChatPort;
import com.agenthub.application.port.out.repositories.AgentRepository;
import com.agenthub.application.port.out.repositories.SessionRepository;
import com.agenthub.common.utils.RandomUtils;
import com.agenthub.domain.exception.NotFoundException;
import com.agenthub.domain.model.agent.AbstractReActAgent;
import com.agenthub.domain.model.agent.AgentMessage;
import com.agenthub.domain.model.agent.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
        saveMessage(user(sessionId, userMessage));
        return agent.streamMessages(sessionId, userMessage)
                .doOnNext(msg -> appendMessages(sessionId, responseBuilder, msg))
                .onErrorResume(throwable -> Flux.just(handlerThrowable(sessionId, throwable)))
                .doFinally(signal -> finallyHandleMessage(sessionId, responseBuilder));
    }

    @Override
    public void interrupt(String agentId, String sessionId) {
        AbstractReActAgent agent = getAgent(agentId, sessionId);
        if (agent != null) agent.interrupt();
    }

    private AbstractReActAgent getAgent(String agentId, String sessionId) {
        return agentPoolUseCase.getAgent(agentId, sessionId);
    }

    private AgentMessage handlerThrowable(String sessionId, Throwable throwable) {
        String treadId = RandomUtils.randomId();
        log.error("Agent session [{}] stream messages [{}] throwable: ", sessionId, treadId, throwable);
        String errorMessage = "发生错误 [" + treadId + "] : " + throwable.getMessage();
        saveMessage(system(sessionId, errorMessage));
        return new AgentMessage(AgentMessage.MessageType.SYSTEM, errorMessage);
    }

    /**
     * 追加响应内容。
     */
    private void appendMessages(String sessionId, StringBuilder builder, AgentMessage message) {
        switch (message.getMessageType()) {
            case ASSISTANT -> handleAssistantMessage(sessionId, builder, message);
            case TOOL -> saveMessage(tool(sessionId, toJson(message.getResponses())));
            case SYSTEM -> saveMessage(system(sessionId, message.getText()));
            default -> {
            }
        }
    }

    private void handleAssistantMessage(String sessionId, StringBuilder builder, AgentMessage msg) {
        if (msg.getText() != null) {
            builder.append(msg.getText());
        }
        if (msg.getToolCalls() != null && !msg.getToolCalls().isEmpty()) {
            saveMessage(flushPendingText(sessionId, builder));
            saveMessage(assistant(sessionId, toJson(msg.getToolCalls())));
        }
        if ("STOP".equalsIgnoreCase((String) msg.getMetadata().getOrDefault("finishReason", ""))) {
            saveMessage(flushPendingText(sessionId, builder));
        }
    }

    private void finallyHandleMessage(String sessionId, StringBuilder responseBuilder) {
        saveMessage(flushPendingText(sessionId, responseBuilder));
    }

    private ChatMessage flushPendingText(String sessionId, StringBuilder builder) {
        String text = builder.toString().strip().replaceFirst("^\n", "").replaceFirst("\n$", "");
        if (!text.isEmpty()) {
            builder.delete(0, builder.length());
            return assistant(sessionId, text);
        }
        return null;
    }

    /**
     * 保存助手消息。
     */
    private void saveMessage(ChatMessage message) {
        if (message != null) {
            sessionRepository.saveMessage(message);
        }
    }

    /**
     * 验证Agent存在。
     */
    private void validateAgentExists(String agentId) {
        agentRepository.findById(agentId)
                .orElseThrow(() -> new NotFoundException("Agent not found: " + agentId));
    }


}
