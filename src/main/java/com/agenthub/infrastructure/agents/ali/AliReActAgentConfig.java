package com.agenthub.infrastructure.agents.ali;

import com.alibaba.cloud.ai.graph.agent.hook.Hook;
import com.alibaba.cloud.ai.graph.agent.interceptor.Interceptor;
import com.alibaba.cloud.ai.graph.checkpoint.BaseCheckpointSaver;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;

/**
 * Agent运行时配置值对象。
 */
public record AliReActAgentConfig(
        String name,
        ChatModel chatModel,
        String systemPrompt,
        List<ToolCallback> tools,
        List<Hook> hooks,
        List<Interceptor> interceptors,
        BaseCheckpointSaver saver
) {
}
