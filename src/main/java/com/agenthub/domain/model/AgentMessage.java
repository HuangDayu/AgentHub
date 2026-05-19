package com.agenthub.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.MimeType;

import java.net.URI;
import java.util.*;

/**
 * Agent消息领域模型，映射Spring AI {@link Message} 的四种实现：
 * <ul>
 *   <li>{@link UserMessage} - 用户消息</li>
 *   <li>{@link AssistantMessage} - 助手消息（含工具调用与媒体附件）</li>
 *   <li>{@link SystemMessage} - 系统消息</li>
 *   <li>{@link ToolResponseMessage} - 工具响应消息</li>
 * </ul>
 *
 * <p>内部定义了 {@link ToolCall}、{@link ToolResponse}、{@link Media} 子类，
 * 避免领域模型对Spring AI框架类型的直接依赖。
 *
 * <p>支持双向转换：{@link #fromMessage(Message)} 与 {@link #toMessage()}。
 *
 * @author huangdayu
 */
@Data
@AllArgsConstructor
public class AgentMessage {

    private MessageType messageType;
    private String text;
    private Map<String, Object> metadata;
    private List<ToolCall> toolCalls;
    private List<ToolResponse> toolResponses;
    private List<Media> media;


    public AgentMessage() {
        this.metadata = new HashMap<>();
        this.toolCalls = new ArrayList<>();
        this.toolResponses = new ArrayList<>();
        this.media = new ArrayList<>();
    }

    public AgentMessage(MessageType messageType, String text) {
        this();
        this.messageType = messageType;
        this.text = text;
    }

    public AgentMessage(MessageType messageType, String text, Map<String, Object> metadata) {
        this();
        this.messageType = messageType;
        this.text = text;
        this.metadata = copyMap(metadata);
    }


    /**
     * 将Spring AI {@link Message} 转换为 {@link AgentMessage}。
     */
    public static AgentMessage fromMessage(Message message) {
        Objects.requireNonNull(message, "message must not be null");

        AgentMessage result = new AgentMessage();
        result.messageType = message.getMessageType();
        result.text = message.getText();
        result.metadata = copyMap(message.getMetadata());

        switch (message) {
            case AssistantMessage msg -> convertFromAssistant(result, msg);
            case ToolResponseMessage msg -> convertFromToolResponse(result, msg);
            default -> { /* UserMessage / SystemMessage: fields already populated */ }
        }

        return result;
    }

    private static void convertFromAssistant(AgentMessage result, AssistantMessage msg) {
        result.toolCalls = msg.getToolCalls() != null
                ? msg.getToolCalls().stream().map(ToolCall::from).toList()
                : List.of();
        result.media = msg.getMedia() != null
                ? msg.getMedia().stream().map(Media::from).toList()
                : List.of();
    }

    private static void convertFromToolResponse(AgentMessage result, ToolResponseMessage msg) {
        result.toolResponses = msg.getResponses() != null
                ? msg.getResponses().stream().map(ToolResponse::from).toList()
                : List.of();
    }


    /**
     * 将当前 {@link AgentMessage} 转换为Spring AI {@link Message}。
     * <p>对于需要携带元数据/工具调用的消息类型使用Builder模式构建。
     */
    public Message toMessage() {
        return switch (messageType) {
            case USER -> new UserMessage(text);
            case SYSTEM -> new SystemMessage(text);
            case ASSISTANT -> buildAssistantMessage();
            case TOOL -> buildToolResponseMessage();
        };
    }

    private AssistantMessage buildAssistantMessage() {
        return AssistantMessage.builder()
                .content(text)
                .properties(nullToEmpty(metadata))
                .toolCalls(resolveToolCalls())
                .media(resolveMedia())
                .build();
    }

    private ToolResponseMessage buildToolResponseMessage() {
        return ToolResponseMessage.builder()
                .responses(resolveToolResponses())
                .metadata(nullToEmpty(metadata))
                .build();
    }

    private List<AssistantMessage.ToolCall> resolveToolCalls() {
        if (toolCalls == null || toolCalls.isEmpty()) return List.of();
        return toolCalls.stream().map(ToolCall::toSpring).toList();
    }

    private List<ToolResponseMessage.ToolResponse> resolveToolResponses() {
        if (toolResponses == null || toolResponses.isEmpty()) return List.of();
        return toolResponses.stream().map(ToolResponse::toSpring).toList();
    }

    private List<org.springframework.ai.content.Media> resolveMedia() {
        if (media == null || media.isEmpty()) return List.of();
        return media.stream().map(Media::toSpring).toList();
    }


    /**
     * 工具调用信息，镜像 {@link AssistantMessage.ToolCall}。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class ToolCall {
        private String id;
        private String type;
        private String name;
        private String arguments;

        public static ToolCall from(AssistantMessage.ToolCall spring) {
            return new ToolCall(spring.id(), spring.type(), spring.name(), spring.arguments());
        }

        public AssistantMessage.ToolCall toSpring() {
            return new AssistantMessage.ToolCall(id, type, name, arguments);
        }

    }

    /**
     * 工具响应信息，镜像 {@link ToolResponseMessage.ToolResponse}。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class ToolResponse {
        private String id;
        private String name;
        private String responseData;

        public static ToolResponse from(ToolResponseMessage.ToolResponse spring) {
            return new ToolResponse(spring.id(), spring.name(), spring.responseData());
        }

        public ToolResponseMessage.ToolResponse toSpring() {
            return new ToolResponseMessage.ToolResponse(id, name, responseData);
        }
    }

    /**
     * 媒体附件信息，镜像 {@link org.springframework.ai.content.Media}。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class Media {
        private String mimeType;
        private Object data;
        private String id;
        private String name;

        public static Media from(org.springframework.ai.content.Media springMedia) {
            return new Media(
                    springMedia.getMimeType().toString(),
                    springMedia.getData(),
                    springMedia.getId(),
                    springMedia.getName()
            );
        }

        public org.springframework.ai.content.Media toSpring() {
            MimeType mt = MimeType.valueOf(mimeType);
            org.springframework.ai.content.Media.Builder builder = org.springframework.ai.content.Media.builder()
                    .mimeType(mt);

            if (data instanceof byte[] bytes) {
                builder.data(new ByteArrayResource(bytes));
            } else if (data != null) {
                builder.data(URI.create(data.toString()));
            }

            if (id != null) builder.id(id);
            if (name != null) builder.name(name);

            return builder.build();
        }

    }


    private static Map<String, Object> copyMap(Map<String, Object> source) {
        return source != null ? new HashMap<>(source) : new HashMap<>();
    }

    private static <K, V> Map<K, V> nullToEmpty(Map<K, V> map) {
        return map != null ? map : Collections.emptyMap();
    }
}
