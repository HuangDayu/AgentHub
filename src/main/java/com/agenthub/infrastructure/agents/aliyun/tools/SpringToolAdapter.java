package com.agenthub.infrastructure.agents.aliyun.tools;

import com.agenthub.domain.model.agent.ReActAgentContext;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.message.ToolResultBlock;
import io.agentscope.core.tool.AgentTool;
import io.agentscope.core.tool.ToolCallParam;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import reactor.core.publisher.Mono;
import tools.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Map;

import static com.agenthub.common.constants.AgentConstants.AGENT_CONTEXT_KEY;
import static org.springframework.ai.util.json.JsonParser.fromJson;
import static org.springframework.ai.util.json.JsonParser.toJson;

/**
 * Spring AI 工具适配器，将 Spring AI 的 ToolCallback 适配为 AgentScope 的 AgentTool。
 */
public class SpringToolAdapter implements AgentTool {

    private final ToolCallback toolCallback;

    public SpringToolAdapter(ToolCallback toolCallback) {
        this.toolCallback = toolCallback;
    }

    @Override
    public String getName() {
        return toolCallback.getToolDefinition().name();
    }

    @Override
    public String getDescription() {
        return toolCallback.getToolDefinition().description();
    }

    @Override
    public Map<String, Object> getParameters() {
        return fromJson(toolCallback.getToolDefinition().inputSchema(), new TypeReference<>() {
        });
    }

    @Override
    public Mono<ToolResultBlock> callAsync(ToolCallParam param) {
        ReActAgentContext reActAgentContext = param.getContext().get(AGENT_CONTEXT_KEY, ReActAgentContext.class);
        String result = toolCallback.call(toJson(param.getInput()), new ToolContext(Map.of(AGENT_CONTEXT_KEY, reActAgentContext)));
        TextBlock textBlock = TextBlock.builder().text(result).build();
        return Mono.just(new ToolResultBlock(param.getToolUseBlock().getId(), param.getToolUseBlock().getName(), List.of(textBlock)));
    }
}
