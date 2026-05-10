package com.agenthub.infrastructure.agents.ali;

import com.alibaba.cloud.ai.graph.agent.hook.Hook;
import com.alibaba.cloud.ai.graph.agent.interceptor.Interceptor;
import com.alibaba.cloud.ai.graph.checkpoint.BaseCheckpointSaver;
import com.alibaba.cloud.ai.graph.store.stores.BaseStore;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AliReActAgentConfig {
    private String name;
    private ChatModel chatModel;
    private String systemPrompt;
    private List<ToolCallback> tools;
    private List<Hook> hooks;
    private List<Interceptor> interceptors;
    private BaseCheckpointSaver saver;
    private BaseStore store;
    private Map<String, Object> toolContext;
}
