package com.agenthub.infrastructure.agents.ali.hook;

import com.alibaba.cloud.ai.graph.agent.hook.AgentHook;
import org.springframework.stereotype.Component;

/**
 * Agent Hook工厂，提供常用Hook实例。
 */
@Component
public class AgentHookFactory {

    public AgentHook loggingHook() {
        return new LoggingHook();
    }


}
