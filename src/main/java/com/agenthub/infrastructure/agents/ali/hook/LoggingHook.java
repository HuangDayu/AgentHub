package com.agenthub.infrastructure.agents.ali.hook;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.AgentHook;
import com.alibaba.cloud.ai.graph.agent.hook.HookPosition;
import com.alibaba.cloud.ai.graph.agent.hook.HookPositions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author huangdayu
 */
@HookPositions({HookPosition.BEFORE_AGENT, HookPosition.AFTER_AGENT})
public class LoggingHook extends AgentHook {
    private static final Logger log = LoggerFactory.getLogger(LoggingHook.class);

    @Override
    public String getName() {
        return "LoggingHook";
    }

    @Override
    public CompletableFuture<Map<String, Object>> beforeAgent(
            OverAllState state, RunnableConfig config) {
        log.info("Agent 开始执行");
        return CompletableFuture.completedFuture(config.context());
    }

    @Override
    public CompletableFuture<Map<String, Object>> afterAgent(
            OverAllState state, RunnableConfig config) {
        log.info("Agent 执行完成");
        return CompletableFuture.completedFuture(config.context());
    }
}
