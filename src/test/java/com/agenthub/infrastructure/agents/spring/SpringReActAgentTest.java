package com.agenthub.infrastructure.agents.spring;

import com.agenthub.application.factory.TeamAgentFactory;
import com.agenthub.domain.enums.AgentLifecycleState;
import com.agenthub.domain.model.agent.Agent;
import com.agenthub.domain.model.agent.AgentMessage;
import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.domain.model.agent.ReActAgentWorkspace;
import com.agenthub.domain.model.strategy.ModelStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SpringReActAgent 生命周期和状态单元测试。
 */
@ExtendWith(MockitoExtension.class)
class SpringReActAgentTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private TeamAgentFactory teamAgentFactory;

    private SpringReActAgent agent;

    private ReActAgentContext context;

    @BeforeEach
    void setUp() {
        context = buildTestContext();
        agent = new SpringReActAgent(context, chatClient, teamAgentFactory);
    }

    @Test
    void shouldStartInCreatedState() {
        assertEquals(AgentLifecycleState.CREATED, agent.getState());
    }

    @Test
    void shouldTransitionToRunningOnInit() {
        agent.init();

        assertEquals(AgentLifecycleState.RUNNING, agent.getState());
    }

    @Test
    void shouldReturnAgentName() {
        assertEquals("test-agent", agent.getName());
    }

    @Test
    void shouldReturnNativeChatClient() {
        assertSame(chatClient, agent.getNativeAgent());
    }

    @Test
    void shouldReturnContext() {
        assertSame(context, agent.getContext());
    }

    @Test
    void shouldTransitionToStoppedOnInterrupt() {
        agent.interrupt();

        assertEquals(AgentLifecycleState.STOPPED, agent.getState());
    }

    @Test
    void shouldReturnEmptyTeams() {
        assertTrue(agent.teams().isEmpty());
    }

    private ReActAgentContext buildTestContext() {
        Agent domainAgent = new Agent();
        domainAgent.setName("test-agent");
        domainAgent.setId("agent-1");

        ModelStrategy modelStrategy = ModelStrategy.create("ws-1", "default");

        ReActAgentContext ctx = new ReActAgentContext();
        ctx.setAgent(domainAgent);
        ctx.setSessionId("session-1");
        ctx.setSystemPrompt("你是一个测试助手。");
        ctx.setChatModelId("model-1");
        ctx.setModelStrategy(modelStrategy);
        ctx.setWorkspace(new ReActAgentWorkspace());
        return ctx;
    }
}
