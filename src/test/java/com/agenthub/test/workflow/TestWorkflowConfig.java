package com.agenthub.test.workflow;

import com.agenthub.application.port.out.agent.AgentChatPort;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

/**
 * 工作流测试专用配置。
 * 提供一个 @Primary 的 AgentChatPort mock bean，使工作流测试不依赖真实 Agent 调用。
 */
@TestConfiguration
public class TestWorkflowConfig {

    @Bean
    @Primary
    public AgentChatPort testAgentChatPort() {
        return mock(AgentChatPort.class);
    }
}
