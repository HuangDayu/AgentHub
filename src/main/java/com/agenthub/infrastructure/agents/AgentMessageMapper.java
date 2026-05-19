package com.agenthub.infrastructure.agents;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.domain.model.AgentMessage;
import org.springframework.ai.chat.messages.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.MimeType;

import java.net.URI;
import java.util.*;

/**
 * @author huangdayu
 */
public final class AgentMessageMapper {

    /**
     * 将Spring AI {@link Message} 转换为 {@link AgentMessage}。
     */
    public static AgentMessage fromMessage(Message message) {
        Objects.requireNonNull(message, "message must not be null");

        AgentMessage result = new AgentMessage();
        result.setMessageType(AgentMessage.MessageType.valueOf(message.getMessageType().name()));
        result.setText(message.getText());
        result.setMetadata(copyMap(message.getMetadata()));

        switch (message) {
            case AssistantMessage msg -> convertFromAssistant(result, msg);
            case ToolResponseMessage msg -> convertFromToolResponse(result, msg);
            default -> { /* UserMessage / SystemMessage: fields already populated */ }
        }

        return result;
    }


    private static void convertFromAssistant(AgentMessage result, AssistantMessage msg) {
        result.setToolCalls(msg.getToolCalls() != null
                ? msg.getToolCalls().stream().map(AgentMessageMapper::fromToolCall).toList()
                : List.of());
        result.setMedia(msg.getMedia() != null
                ? msg.getMedia().stream().map(AgentMessageMapper::fromMedia).toList()
                : List.of());
    }

    private static void convertFromToolResponse(AgentMessage result, ToolResponseMessage msg) {
        result.setToolResponses(msg.getResponses() != null
                ? msg.getResponses().stream().map(AgentMessageMapper::fromToolResponse).toList()
                : List.of());
    }

    /**
     * 将当前 {@link AgentMessage} 转换为Spring AI {@link Message}。
     * <p>对于需要携带元数据/工具调用的消息类型使用Builder模式构建。
     */
    public static Message toMessage(AgentMessage agentMessage) {
        return switch (agentMessage.getMessageType()) {
            case USER -> new UserMessage(agentMessage.getText());
            case SYSTEM -> new SystemMessage(agentMessage.getText());
            case ASSISTANT -> buildAssistantMessage(agentMessage);
            case TOOL -> buildToolResponseMessage(agentMessage);
        };
    }

    private static AssistantMessage buildAssistantMessage(AgentMessage agentMessage) {
        return AssistantMessage.builder()
                .content(agentMessage.getText())
                .properties(nullToEmpty(agentMessage.getMetadata()))
                .toolCalls(resolveToolCalls(agentMessage))
                .media(resolveMedia(agentMessage))
                .build();
    }

    private static ToolResponseMessage buildToolResponseMessage(AgentMessage agentMessage) {
        return ToolResponseMessage.builder()
                .responses(resolveToolResponses(agentMessage))
                .metadata(nullToEmpty(agentMessage.getMetadata()))
                .build();
    }

    private static List<AssistantMessage.ToolCall> resolveToolCalls(AgentMessage agentMessage) {
        List<AgentMessage.ToolCall> toolCalls = agentMessage.getToolCalls();
        if (toolCalls == null || toolCalls.isEmpty()) return List.of();
        return toolCalls.stream().map(AgentMessageMapper::toSpringToolCall).toList();
    }

    private static List<ToolResponseMessage.ToolResponse> resolveToolResponses(AgentMessage agentMessage) {
        List<AgentMessage.ToolResult> toolResponses = agentMessage.getToolResponses();
        if (toolResponses == null || toolResponses.isEmpty()) return List.of();
        return toolResponses.stream().map(AgentMessageMapper::toSpringToolResponse).toList();
    }

    private static List<org.springframework.ai.content.Media> resolveMedia(AgentMessage agentMessage) {
        List<AgentMessage.Media> media = agentMessage.getMedia();
        if (media == null || media.isEmpty()) return List.of();
        return media.stream().map(AgentMessageMapper::toSpringMedia).toList();
    }


    private static Map<String, Object> copyMap(Map<String, Object> source) {
        return source != null ? new HashMap<>(source) : new HashMap<>();
    }

    private static <K, V> Map<K, V> nullToEmpty(Map<K, V> map) {
        return map != null ? map : Collections.emptyMap();
    }

    private static AgentMessage.Media fromMedia(org.springframework.ai.content.Media springMedia) {
        return new AgentMessage.Media(
                springMedia.getMimeType().toString(),
                springMedia.getData(),
                springMedia.getId(),
                springMedia.getName()
        );
    }

    private static org.springframework.ai.content.Media toSpringMedia(AgentMessage.Media media) {
        MimeType mt = MimeType.valueOf(media.getMimeType());
        org.springframework.ai.content.Media.Builder builder = org.springframework.ai.content.Media.builder()
                .mimeType(mt);

        if (media.getData() instanceof byte[] bytes) {
            builder.data(new ByteArrayResource(bytes));
        } else if (media.getData() != null) {
            builder.data(URI.create(media.getData().toString()));
        }

        if (media.getId() != null) builder.id(media.getId());
        if (media.getName() != null) builder.name(media.getName());

        return builder.build();
    }

    private static AgentMessage.ToolResult fromToolResponse(ToolResponseMessage.ToolResponse spring) {
        return new AgentMessage.ToolResult(spring.id(), spring.name(), spring.responseData());
    }

    private static ToolResponseMessage.ToolResponse toSpringToolResponse(AgentMessage.ToolResult toolResponse) {
        return new ToolResponseMessage.ToolResponse(toolResponse.getId(), toolResponse.getName(), toolResponse.getResponseData());
    }


    private static AgentMessage.ToolCall fromToolCall(AssistantMessage.ToolCall spring) {
        return new AgentMessage.ToolCall(spring.id(), spring.type(), spring.name(), spring.arguments());
    }

    private static AssistantMessage.ToolCall toSpringToolCall(AgentMessage.ToolCall toolCall) {
        return new AssistantMessage.ToolCall(toolCall.getId(), toolCall.getType(), toolCall.getName(), toolCall.getArguments());
    }
}
