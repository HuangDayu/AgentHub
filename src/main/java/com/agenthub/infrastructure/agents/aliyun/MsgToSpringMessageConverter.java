package com.agenthub.infrastructure.agents.aliyun;

import io.agentscope.core.message.Msg;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.List;

/**
 * 将 AgentScope {@link Msg} 转换为 Spring AI {@link Message}。
 */
final class MsgToSpringMessageConverter {

    private MsgToSpringMessageConverter() {
    }

    static Message convert(Msg msg) {
        return switch (msg.getRole()) {
            case USER -> new UserMessage(msg.getTextContent());
            case SYSTEM -> new SystemMessage(msg.getTextContent());
            case ASSISTANT -> buildAssistantMessage(msg);
            case TOOL -> buildToolResponse(msg);
        };
    }

    private static AssistantMessage buildAssistantMessage(Msg msg) {
        return AssistantMessage.builder()
                .content(msg.getTextContent())
                .build();
    }

    private static ToolResponseMessage buildToolResponse(Msg msg) {
        return ToolResponseMessage.builder()
                .responses(List.of(
                        new ToolResponseMessage.ToolResponse("", "", msg.getTextContent())))
                .build();
    }

}
