package com.agenthub.infrastructure.agents.subagent;

import com.agenthub.application.port.out.repositories.SessionRepository;
import com.agenthub.common.utils.RandomUtils;
import com.agenthub.domain.model.agent.AbstractReActAgent;
import com.agenthub.domain.model.agent.AgentMessage;
import com.agenthub.domain.model.agent.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

import static com.agenthub.domain.model.agent.ChatMessage.*;
import static org.springframework.ai.util.json.JsonParser.toJson;

/**
 * @author huangdayu
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AgentStreamExecutor {


    private final SessionRepository sessionRepository;

    public Flux<AgentMessage> streamMessages(AbstractReActAgent agent, String sessionId, String userMessage) {
        StringBuilder responseBuilder = new StringBuilder();
        saveMessage(user(sessionId, userMessage));
        return agent.streamMessages(List.of(new AgentMessage(AgentMessage.MessageType.USER, userMessage)))
                .doOnNext(msg -> appendMessages(sessionId, responseBuilder, msg))
                .onErrorResume(throwable -> Flux.just(handlerThrowable(sessionId, throwable)))
                .doFinally(signal -> finallyHandleMessage(sessionId, responseBuilder));
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

}
