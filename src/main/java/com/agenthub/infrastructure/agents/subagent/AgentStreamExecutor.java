package com.agenthub.infrastructure.agents.subagent;

import com.agenthub.application.port.out.repositories.SessionRepository;
import com.agenthub.application.usecase.ChatAttachmentUseCase;
import com.agenthub.common.utils.RandomUtils;
import com.agenthub.domain.model.agent.AbstractReActAgent;
import com.agenthub.domain.model.agent.AgentMessage;
import com.agenthub.domain.model.agent.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
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
    private final ChatAttachmentUseCase chatAttachmentUseCase;

    public Flux<AgentMessage> streamMessages(AgentStreamCommand command) {
        StringBuilder responseBuilder = new StringBuilder();
        saveMessage(user(command.getSessionId(), command.getUserMessage()));
        return command.getAgent().streamMessages(buildMessages(command))
                .filter(this::shouldEmit)
                .doOnNext(msg -> appendMessages(command.getSessionId(), responseBuilder, msg))
                .onErrorResume(throwable -> Flux.just(handlerThrowable(command.getSessionId(), throwable)))
                .doFinally(signal -> finallyHandleMessage(command.getSessionId(), responseBuilder));
    }

    private List<AgentMessage> buildMessages(AgentStreamCommand command) {
        List<AgentMessage> messages = new ArrayList<>();
        messages.add(new AgentMessage(AgentMessage.MessageType.USER, command.getUserMessage()));
        appendFileContext(messages, command.getFilePaths());
        return messages;
    }

    private void appendFileContext(List<AgentMessage> messages, List<String> filePaths) {
        if (filePaths == null || filePaths.isEmpty()) return;
        String content = chatAttachmentUseCase.readFilesContent(filePaths);
        if (content != null && !content.isEmpty()) {
            messages.add(new AgentMessage(AgentMessage.MessageType.ASSISTANT, content));
        }
    }

    private AgentMessage handlerThrowable(String sessionId, Throwable throwable) {
        String treadId = RandomUtils.randomId();
        log.error("Agent session [{}] stream messages [{}] throwable: ", sessionId, treadId, throwable);
        String errorMessage = "发生错误 [" + treadId + "] : " + throwable.getMessage();
        saveMessage(system(sessionId, errorMessage));
        return new AgentMessage(AgentMessage.MessageType.SYSTEM, errorMessage);
    }

    private boolean shouldEmit(AgentMessage message) {
        return !isFragmentToolCall(message);
    }

    private boolean isFragmentToolCall(AgentMessage message) {
        if (message.getToolCalls() == null) return false;
        return message.getToolCalls().stream().anyMatch(this::isFragmentToolCall);
    }

    private boolean isFragmentToolCall(AgentMessage.ToolCall call) {
        return "fragment".equals(call.getName()) || "__fragment__".equals(call.getName());
    }

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

    private void saveMessage(ChatMessage message) {
        if (message != null) {
            sessionRepository.saveMessage(message);
        }
    }
}
