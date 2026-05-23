package com.agenthub.domain.model.agent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Agent消息领域模型，映射Spring AI {@link Message} 的四种实现：
 * <ul>
 *   <li>{@link UserMessage} - 用户消息</li>
 *   <li>{@link AssistantMessage} - 助手消息（含工具调用与媒体附件）</li>
 *   <li>{@link SystemMessage} - 系统消息</li>
 *   <li>{@link ToolResponseMessage} - 工具响应消息</li>
 * </ul>
 *
 * <p>内部定义了 {@link ToolCall}、{@link ToolResult}、{@link Media} 子类，
 * 避免领域模型对Spring AI框架类型的直接依赖。
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
    private List<ToolResult> responses;
    private List<Media> media;
    private ChatUsage chatUsage;


    public AgentMessage() {
        this.metadata = new HashMap<>();
        this.toolCalls = new ArrayList<>();
        this.responses = new ArrayList<>();
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
        this.metadata = metadata;
    }

    public AgentMessage(MessageType messageType, ChatUsage chatUsage, Map<String, Object> metadata) {
        this();
        this.messageType = messageType;
        this.chatUsage = chatUsage;
        this.metadata = metadata;
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


    }

    /**
     * 工具响应信息，镜像 {@link ToolResponseMessage.ToolResponse}。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class ToolResult {
        private String id;
        private String name;
        private String responseData;
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


    }

    public static enum MessageType {
        USER("user"),
        ASSISTANT("assistant"),
        SYSTEM("system"),
        TOOL("tool");

        private final String value;

        MessageType(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChatUsage {
        private int inputTokens;
        private int outputTokens;
        private double time;
        private int totalTokens;
    }


}
