package com.agenthub.infrastructure.agents.ali;

import com.alibaba.cloud.ai.graph.agent.hook.Hook;
import com.alibaba.cloud.ai.graph.agent.interceptor.Interceptor;
import com.alibaba.cloud.ai.graph.checkpoint.BaseCheckpointSaver;
import com.alibaba.cloud.ai.graph.store.stores.BaseStore;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.Map;

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
        BaseCheckpointSaver saver,
        BaseStore store,
        Map<String, Object> toolContext
) {
}
