package com.agenthub.application.usecase;

import com.agenthub.application.port.out.agent.AgentPort;
import com.agenthub.application.port.out.repositories.AgentRepository;
import com.agenthub.application.port.out.repositories.SessionRepository;
import com.agenthub.domain.exception.NotFoundException;
import com.agenthub.domain.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.LinkedList;
import java.util.List;

import static com.agenthub.domain.model.ChatMessage.*;
import static org.springframework.ai.util.json.JsonParser.toJson;

/**
 * Agent对话用例，处理Agent对话和Session管理。
 *
 * @author huangdayu
 */
@Component
@RequiredArgsConstructor
public class AgentChatUseCase {

    private final AgentPort agentPort;
    private final AgentRepository agentRepository;
    private final SessionRepository sessionRepository;


    /**
     * 同步对话。
     */
    public String chat(String agentId, String sessionId, String userMessage) {
        sessionRepository.existSession(sessionId, agentId);
        List<ChatMessage> messages = new LinkedList<>();
        messages.add(user(sessionId, userMessage));
        AssistantMessage response = agentPort.call(agentId, sessionId, userMessage);
        messages.add(assistant(sessionId, response.getText()));
        sessionRepository.saveMessages(messages);
        return response.getText();
    }

    /**
     * 流式对话。
     */
    public Flux<Message> streamChat(String agentId, String sessionId, String userMessage) {
        sessionRepository.existSession(sessionId, agentId);
        StringBuilder responseBuilder = new StringBuilder();
        List<ChatMessage> messages = new LinkedList<>();
        messages.add(user(sessionId, userMessage));
        return agentPort.streamMessages(agentId, sessionId, userMessage)
                .doOnNext(msg -> appendMessages(sessionId, messages, responseBuilder, msg))
                .onErrorResume(throwable -> {
                    String errorMessage = "对话过程中发生错误: " + throwable.getMessage();
                    messages.add(system(sessionId, errorMessage));
                    return Flux.just(new SystemMessage(errorMessage));
                })
                .doFinally(signal -> saveMessages(messages));
    }


    /**
     * 追加响应内容。
     */
    private void appendMessages(String sessionId, List<ChatMessage> messages, StringBuilder builder, Message message) {
        if (message instanceof AssistantMessage assistantMessage) {
            if (assistantMessage.getText() != null) {
                builder.append(assistantMessage.getText());
            }
            if (!assistantMessage.getToolCalls().isEmpty()) {
                appendMessage(sessionId, messages, builder);
                messages.add(assistant(sessionId, toJson(assistantMessage.getToolCalls())));
            }
            if ("STOP".equalsIgnoreCase((String) assistantMessage.getMetadata().getOrDefault("finishReason", ""))) {
                appendMessage(sessionId, messages, builder);
            }
        }
        if (message instanceof ToolResponseMessage toolResponseMessage) {
            messages.add(tool(sessionId, toJson(toolResponseMessage.getResponses())));
        }
        if (message instanceof SystemMessage systemMessage) {
            messages.add(system(sessionId, systemMessage.getText()));
        }
    }

    private void appendMessage(String sessionId, List<ChatMessage> messages, StringBuilder builder) {
        String text = builder.toString();
        if (!text.isEmpty() && !"\n".equals(text)) {
            if (text.startsWith("\n")) {
                text = text.replaceFirst("\n", "");
            }
            if (text.endsWith("\n")) {
                text = text.substring(0, text.length() - 1);
            }
            messages.add(assistant(sessionId, text));
        }
        builder.delete(0, builder.length());
    }

    /**
     * 保存助手消息。
     */
    private void saveMessages(List<ChatMessage> messages) {
        sessionRepository.saveMessages(messages);
    }

    /**
     * 中断Agent执行。
     */
    public void interrupt(String agentId, String sessionId) {
        agentPort.interrupt(agentId, sessionId);
    }

    /**
     * 验证Agent存在。
     */
    private void validateAgentExists(String agentId) {
        agentRepository.findById(agentId)
                .orElseThrow(() -> new NotFoundException("Agent not found: " + agentId));
    }


}
