package com.agenthub.infrastructure.agents.aliyun;

import io.agentscope.core.message.ContentBlock;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.model.ChatResponse;
import io.agentscope.core.model.ChatUsage;
import io.agentscope.core.model.GenerateOptions;
import io.agentscope.core.model.Model;
import io.agentscope.core.model.ToolSchema;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.DefaultToolCallingChatOptions;
import org.springframework.ai.tool.ToolCallback;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 桥接 Spring AI 的 {@link ChatModel} 到 AgentScope 的 {@link Model}。
 * <p>
 * 将 AgentScope 的消息格式转换为 Spring AI 格式，委托底层模型执行推理，
 * 再将 Spring AI 的响应转换回 AgentScope 的 {@link ChatResponse}。
 * 工具通过 Spring AI 的 internalToolExecutionEnabled 机制执行。
 */
public class AgentScopeSpringModelAdapter implements Model {

    private final String modelName;
    private final ChatModel chatModel;
    private final List<ToolCallback> toolCallbacks;

    public AgentScopeSpringModelAdapter(String modelName, ChatModel chatModel, List<ToolCallback> toolCallbacks) {
        this.modelName = modelName;
        this.chatModel = chatModel;
        this.toolCallbacks = toolCallbacks;
    }

    @Override
    public Flux<ChatResponse> stream(List<Msg> messages, List<ToolSchema> tools,
                                     GenerateOptions options) {
        var springMessages = messages.stream()
                .map(MsgToSpringMessageConverter::convert)
                .collect(Collectors.toList());

        Prompt prompt = buildPrompt(springMessages);
        return chatModel.stream(prompt)
                .map(AgentScopeSpringModelAdapter::convertChatResponse);
    }

    private Prompt buildPrompt(List<org.springframework.ai.chat.messages.Message> messages) {
        if (toolCallbacks == null || toolCallbacks.isEmpty()) {
            return new Prompt(messages);
        }
        var toolOptions = DefaultToolCallingChatOptions.builder()
                .toolCallbacks(toolCallbacks)
                .internalToolExecutionEnabled(true)
                .build();
        return new Prompt(messages, toolOptions);
    }

    @Override
    public String getModelName() {
        return modelName;
    }

    /**
     * 将 Spring AI {@link org.springframework.ai.chat.model.ChatResponse} 转换为
     * AgentScope {@link ChatResponse}。
     */
    private static ChatResponse convertChatResponse(
            org.springframework.ai.chat.model.ChatResponse springResponse) {
        var result = springResponse.getResult();
        if (result == null || result.getOutput() == null) {
            return buildEmptyChatResponse();
        }
        return buildChatResponse(result.getOutput().getText());
    }

    private static ChatResponse buildEmptyChatResponse() {
        return ChatResponse.builder()
                .id(null)
                .content(List.of(TextBlock.builder().text("").build()))
                .usage(new ChatUsage(0, 0, 0.0))
                .finishReason(null)
                .build();
    }

    private static ChatResponse buildChatResponse(String text) {
        List<ContentBlock> content = List.of(TextBlock.builder().text(text != null ? text : "").build());
        return ChatResponse.builder()
                .id(null)
                .content(content)
                .usage(new ChatUsage(0, 0, 0.0))
                .finishReason("stop")
                .build();
    }

}
