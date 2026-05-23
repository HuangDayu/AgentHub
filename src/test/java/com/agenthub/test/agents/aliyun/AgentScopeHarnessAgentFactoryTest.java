package com.agenthub.test.agents.aliyun;

import com.agenthub.domain.enums.AgentToolType;
import com.agenthub.domain.model.agent.Agent;
import com.agenthub.domain.model.agent.AbstractReActAgent;
import com.agenthub.domain.model.agent.AgentMessage;
import com.agenthub.domain.model.agent.AgentToolInfo;
import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.domain.model.agent.ReActAgentWorkspace;
import com.agenthub.domain.model.strategy.ModelStrategy;
import com.agenthub.infrastructure.agents.aliyun.AgentScopeHarnessAgent;
import com.agenthub.infrastructure.agents.aliyun.AgentScopeReActAgentConfig;
import com.agenthub.infrastructure.agents.aliyun.AgentScopeHarnessAgentFactory;
import com.agenthub.infrastructure.agents.aliyun.model.AgentScopeModelFactoryRegistry;
import com.agenthub.infrastructure.factory.SpringShareObjectFactory;
import com.agenthub.infrastructure.tools.AgentToolsFactory;
import io.agentscope.core.agent.Event;
import io.agentscope.core.agent.EventType;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import com.agenthub.infrastructure.agents.aliyun.AgentScopeTeamAgentFactory;
import io.agentscope.harness.agent.HarnessAgent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * 验证 {@link AgentScopeHarnessAgentFactory} 的集成逻辑。
 */
@ExtendWith(MockitoExtension.class)
public class AgentScopeHarnessAgentFactoryTest {

    @Mock
    private SpringShareObjectFactory springShareObjectFactory;

    @Mock
    private AgentToolsFactory agentToolsFactory;

    @Mock
    private AgentScopeModelFactoryRegistry registry;

    @Mock
    private AgentScopeTeamAgentFactory teamAgentFactory;

    @Mock
    private ChatModel chatModel;

    private Agent agent;
    private ReActAgentContext context;

    @BeforeEach
    void setUp() {
        agent = new Agent();
        agent.setName("test-agent");
        agent.setDescription("A test agent");

        context = ReActAgentContext.builder()
                .agent(agent)
                .sessionId("sess-001")
                .chatModelId("model-1")
                .systemPrompt("You are helpful")
                .tools(List.of(new AgentToolInfo(AgentToolType.SYSTEM_TOOL)))
                .modelStrategy(ModelStrategy.create("ws-1", "default"))
                .workspace(ReActAgentWorkspace.builder()
                        .rootPath(Path.of("/tmp/test-workspace"))
                        .build())
                .build();

        // teamAgentFactory is mocked via @Mock
    }

    @Test
    public void shouldResolveModelPreferringNative() {
        var nativeModel = org.mockito.Mockito.mock(io.agentscope.core.model.Model.class);
        when(registry.getOrCreateModel("model-1")).thenReturn(nativeModel);

        var teamAgentFactoryProvider = org.mockito.Mockito.mock(
                org.springframework.beans.factory.ObjectProvider.class);
        when(teamAgentFactoryProvider.getObject()).thenReturn(teamAgentFactory);
        
        var factory = new AgentScopeHarnessAgentFactory(
                springShareObjectFactory, agentToolsFactory, registry, 
                new com.agenthub.infrastructure.agents.aliyun.memory.MemoryConfigFactory(),
                new com.agenthub.infrastructure.agents.aliyun.tools.ToolkitFactory(),
                new com.agenthub.infrastructure.agents.aliyun.session.SessionFactory(),
                new com.agenthub.infrastructure.agents.aliyun.workspace.WorkspaceManagerFactory(),
                new com.agenthub.infrastructure.agents.aliyun.filesystem.FilesystemFactory(),
                new com.agenthub.infrastructure.agents.aliyun.tools.SpringToolToAgentScopeConverter(),
                teamAgentFactoryProvider);

        AbstractReActAgent agentRuntime = factory.create(context);

        assertThat(agentRuntime).isNotNull();
        assertThat(agentRuntime.getName()).isEqualTo("test-agent");
    }

    @Test
    public void agentShouldDelegateCallToHarnessAgent() {
        var harnessAgent = createMockHarnessAgent("mock reply");
        var config = new AgentScopeReActAgentConfig();
        config.setAgent(agent);

        var reActAgent = new AgentScopeHarnessAgent(context, config, null, harnessAgent);

        AgentMessage result = reActAgent.call("sess-001", "hello");

        assertThat(result).isNotNull();
        assertThat(result.getText()).isEqualTo("mock reply");
    }

    @Test
    public void agentShouldDelegateStreamToHarnessAgent() {
        var harnessAgent = createMockHarnessAgentForStream();
        var config = new AgentScopeReActAgentConfig();
        config.setAgent(agent);

        var reActAgent = new AgentScopeHarnessAgent(context, config, null, harnessAgent);

        Flux<AgentMessage> flux = reActAgent.streamMessages("sess-001", "hello");

        StepVerifier.create(flux)
                .assertNext(msg -> assertThat(msg.getText()).isEqualTo("reply"))
                .verifyComplete();
    }

    @Test
    public void agentShouldManageLifecycle() {
        var harnessAgent = createMockHarnessAgent("");
        var config = new AgentScopeReActAgentConfig();
        config.setAgent(agent);

        var reActAgent = new AgentScopeHarnessAgent(context, config, null, harnessAgent);

        assertThat(reActAgent.getState().name()).isEqualTo("CREATED");
        reActAgent.init();
        assertThat(reActAgent.getState().name()).isEqualTo("RUNNING");
        reActAgent.interrupt();
        assertThat(reActAgent.getState().name()).isEqualTo("STOPPED");
    }

    @Test
    public void agentShouldManageTeams() {
        var harnessAgent = createMockHarnessAgent("");
        var config = new AgentScopeReActAgentConfig();
        config.setAgent(agent);

        var reActAgent = new AgentScopeHarnessAgent(context, config, null, harnessAgent);

        assertThat(reActAgent.teams()).isEmpty();
        assertThat(reActAgent.getNativeAgent()).isSameAs(harnessAgent);
    }

    // --- helpers ---

    private HarnessAgent createMockHarnessAgent(String replyText) {
        var harnessAgent = org.mockito.Mockito.mock(HarnessAgent.class);
        var responseMsg = Msg.builder()
                .role(MsgRole.ASSISTANT)
                .textContent(replyText)
                .build();
        lenient().when(harnessAgent.call(any(Msg.class), any(RuntimeContext.class)))
                .thenReturn(Mono.just(responseMsg));
        lenient().when(harnessAgent.stream(any(Msg.class), any(RuntimeContext.class)))
                .thenReturn(Flux.just(new Event(EventType.REASONING, responseMsg, true)));
        return harnessAgent;
    }

    private HarnessAgent createMockHarnessAgentForStream() {
        var harnessAgent = org.mockito.Mockito.mock(HarnessAgent.class);
        var responseMsg = Msg.builder()
                .role(MsgRole.ASSISTANT)
                .textContent("reply")
                .build();
        when(harnessAgent.stream(any(Msg.class), any(RuntimeContext.class)))
                .thenReturn(Flux.just(new Event(EventType.REASONING, responseMsg, true)));
        return harnessAgent;
    }

}
