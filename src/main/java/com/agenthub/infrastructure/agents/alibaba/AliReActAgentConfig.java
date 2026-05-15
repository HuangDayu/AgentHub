package com.agenthub.infrastructure.agents.alibaba;

import com.agenthub.domain.model.Agent;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.Hook;
import com.alibaba.cloud.ai.graph.agent.interceptor.Interceptor;
import com.alibaba.cloud.ai.graph.checkpoint.BaseCheckpointSaver;
import com.alibaba.cloud.ai.graph.store.stores.BaseStore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AliReActAgentConfig {

    private Agent agent;
    private ChatModel chatModel;
    private Advisor[] advisors;
    private ChatOptions chatOptions;
    private String systemPrompt;
    private List<ToolCallback> tools;
    private List<Hook> hooks;
    private List<Interceptor> interceptors;
    private BaseCheckpointSaver saver;
    private BaseStore store;
    private Map<String, Object> toolContext;
    private RunnableConfig runnableConfig;
}
