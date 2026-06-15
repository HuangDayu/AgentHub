package com.agenthub.infrastructure.agents.spring;

import com.agenthub.application.factory.TeamAgentFactory;
import com.agenthub.domain.model.agent.Agent;
import com.agenthub.domain.model.agent.AbstractReActAgent;
import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.domain.model.agent.ReActAgentWorkspace;
import com.agenthub.domain.model.strategy.ModelStrategy;
import com.agenthub.infrastructure.factory.SpringShareObjectFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.DefaultToolDefinition;
import org.springframework.ai.tool.definition.ToolDefinition;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * SpringReActAgentFactory 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class SpringReActAgentFactoryTest {

    @Mock
    private SpringShareObjectFactory springShareObjectFactory;

    @Mock
    private JdbcChatMemoryRepository jdbcChatMemoryRepository;

    @Mock
    private TeamAgentFactory teamAgentFactory;

    @Mock
    private ChatModel chatModel;

    @Mock
    private ToolCallback toolCallback;

    private SpringReActAgentFactory factory;

    @BeforeEach
    void setUp() {
        factory = new SpringReActAgentFactory(
                springShareObjectFactory, jdbcChatMemoryRepository, teamAgentFactory);
    }

    @Test
    void shouldCreateAgentWithValidContext() {
        ReActAgentContext ctx = buildMinimalContext();

        AbstractReActAgent agent = factory.create(ctx);

        assertNotNull(agent);
        assertInstanceOf(SpringReActAgent.class, agent);
        assertEquals("test-agent", agent.getName());
    }

    @Test
    void shouldReturnSpringReActAgent() {
        ReActAgentContext ctx = buildMinimalContext();

        AbstractReActAgent agent = factory.create(ctx);

        assertInstanceOf(SpringReActAgent.class, agent);
    }

    @Test
    void shouldCreateAgentWithToolCallbacks() {
        ReActAgentContext ctx = buildMinimalContext();
        ToolDefinition definition = DefaultToolDefinition.builder()
                .name("test-tool").description("test tool").inputSchema("{}").build();
        when(toolCallback.getToolDefinition()).thenReturn(definition);
        ctx.setToolCallbacks(List.of(toolCallback));

        AbstractReActAgent agent = factory.create(ctx);

        assertNotNull(agent);
        assertInstanceOf(SpringReActAgent.class, agent);
    }

    private ReActAgentContext buildMinimalContext() {
        Agent agent = new Agent();
        agent.setName("test-agent");
        agent.setId("agent-1");

        ModelStrategy modelStrategy = ModelStrategy.create("ws-1", "default");
        modelStrategy.setMaxMessages(50);

        ReActAgentContext ctx = new ReActAgentContext();
        ctx.setAgent(agent);
        ctx.setSessionId("session-1");
        ctx.setChatModelId("default");
        ctx.setSystemPrompt("你是一个测试助手。");
        ctx.setModelStrategy(modelStrategy);
        ctx.setWorkspace(new ReActAgentWorkspace());
        when(springShareObjectFactory.getChatModelByConfigId("default")).thenReturn(chatModel);
        return ctx;
    }
}
