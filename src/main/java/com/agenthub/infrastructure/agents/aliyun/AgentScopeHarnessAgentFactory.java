package com.agenthub.infrastructure.agents.aliyun;

import com.agenthub.application.factory.ReActAgentFactory;
import com.agenthub.application.port.out.etl.EtlDocumentChunkStorePort;
import com.agenthub.application.port.out.rag.RagVectorSearchPort;
import com.agenthub.domain.model.agent.AbstractReActAgent;
import com.agenthub.domain.model.agent.ReActAgentContext;
import com.agenthub.infrastructure.agents.aliyun.filesystem.FilesystemFactory;
import com.agenthub.infrastructure.agents.aliyun.knowledge.AgentScopeKnowledge;
import com.agenthub.infrastructure.agents.aliyun.memory.MemoryConfigFactory;
import com.agenthub.infrastructure.agents.aliyun.model.AgentScopeModelFactoryRegistry;
import com.agenthub.infrastructure.agents.aliyun.session.SessionFactory;
import com.agenthub.infrastructure.agents.aliyun.tools.SpringToolToAgentScopeConverter;
import com.agenthub.infrastructure.agents.aliyun.tools.ToolkitFactory;
import com.agenthub.infrastructure.agents.aliyun.workspace.WorkspaceManagerFactory;
import com.agenthub.infrastructure.context.TenantContextGetter;
import com.agenthub.infrastructure.factory.SpringShareObjectFactory;
import com.agenthub.infrastructure.telemetry.AgentStudioMessageHandler;
import com.agenthub.infrastructure.tools.AgentToolsFactory;
import io.agentscope.core.model.Model;
import io.agentscope.core.rag.Knowledge;
import io.agentscope.core.skill.repository.FileSystemSkillRepository;
import io.agentscope.core.studio.StudioClient;
import io.agentscope.core.studio.StudioConfig;
import io.agentscope.core.studio.StudioMessageHook;
import io.agentscope.core.tool.ToolExecutionContext;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.harness.agent.HarnessAgent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.agenthub.common.constants.AgentConstants.AGENT_CONTEXT_KEY;
import static com.agenthub.common.constants.AgentConstants.THREAD_CONTEXT_KEY;
import static com.agenthub.domain.enums.AgentToolType.MCP_TOOL;
import static com.agenthub.domain.enums.AgentToolType.SYSTEM_TOOL;

/**
 * AgentScope Harness 框架的 Agent 运行时工厂。
 * <p>
 * 根据 {@link ReActAgentContext} 构建 {@link HarnessAgent}，
 * 适配为项目的 {@link AbstractReActAgent} 接口。
 * <p>
 * 模型实例支持两种路径：
 * <ul>
 *   <li><b>AgentScope 原生</b> — 通过 {@link AgentScopeModelFactoryRegistry} 创建</li>
 *   <li><b>Spring AI 桥接</b> — 通过 {@link AgentScopeSpringModelAdapter} 包装</li>
 * </ul>
 */
@Primary
@RequiredArgsConstructor
@Component
public class AgentScopeHarnessAgentFactory implements ReActAgentFactory {

    private static final Logger log = LoggerFactory.getLogger(AgentScopeHarnessAgentFactory.class);

    private final SpringShareObjectFactory springShareObjectFactory;
    private final AgentToolsFactory agentToolsFactory;
    private final AgentScopeModelFactoryRegistry agentScopeModelFactoryRegistry;
    private final MemoryConfigFactory memoryConfigFactory;
    private final RagVectorSearchPort ragVectorSearchPort;
    private final EtlDocumentChunkStorePort etlDocumentChunkStorePort;
    private final ToolkitFactory toolkitFactory;
    private final SessionFactory sessionFactory;
    private final WorkspaceManagerFactory workspaceManagerFactory;
    private final FilesystemFactory filesystemFactory;
    private final SpringToolToAgentScopeConverter toolConverter;
    private final ObjectProvider<AgentScopeTeamAgentFactory> agentScopeTeamAgentFactory;
    private final AgentStudioMessageHandler agentStudioMessageHandler;
    private final TenantContextGetter tenantContextGetter;

    @Override
    public AbstractReActAgent create(ReActAgentContext ctx) {
        AgentScopeReActAgentConfig config = buildConfig(ctx);
        HarnessAgent agent = buildHarnessAgent(config, ctx);
        return new AgentScopeHarnessAgent(ctx, config, agentScopeTeamAgentFactory.getObject(), agent);
    }

    private AgentScopeReActAgentConfig buildConfig(ReActAgentContext ctx) {
        String chatModelId = ctx.getChatModelId();
        Model model = resolveModel(ctx, chatModelId);
        Path workspacePath = ctx.getWorkspace() != null && ctx.getWorkspace().getRootPath() != null
                ? ctx.getWorkspace().getRootPath()
                : Path.of(".agenthub/workspace");
        return new AgentScopeReActAgentConfig(
                ctx.getAgent(), model, ctx.getSystemPrompt(), workspacePath,
                ctx.getWorkspace(), List.of(), null);
    }

    /**
     * 解析模型：优先使用 AgentScope 原生 Model，回退到 Spring AI 桥接。
     */
    private Model resolveModel(ReActAgentContext ctx, String chatModelId) {
        if (chatModelId == null) return null;
        try {
            Model nativeModel = agentScopeModelFactoryRegistry.getOrCreateModel(chatModelId);
            log.info("Using AgentScope native model for configId={}", chatModelId);
            return nativeModel;
        } catch (Exception e) {
            log.info("Falling back to Spring AI bridge for configId={}: {}",
                    chatModelId, e.getMessage());
        }
        var chatModel = springShareObjectFactory.getChatModelByConfigId(chatModelId);
        return new AgentScopeSpringModelAdapter(ctx.getAgent().getName(), chatModel);
    }

    private HarnessAgent buildHarnessAgent(AgentScopeReActAgentConfig config, ReActAgentContext ctx) {
        return HarnessAgent.builder()
                .name(config.getAgent().getName())
                .sysPrompt(config.getSystemPrompt())
                .model(config.getModel())
                .workspace(config.getWorkspacePath())
                .compaction(memoryConfigFactory.createDefaultCompactionConfig())
                .enablePendingToolRecovery(true)
                .hook(resolveStudioMessageHook(config, ctx))
                .toolExecutionContext(resolveToolExecutionContext(ctx))
                .toolResultEviction(memoryConfigFactory.createDefaultToolResultEvictionConfig())
                .toolkit(resolveToolkit(ctx))
                .skillRepository(new FileSystemSkillRepository(ctx.getWorkspace().getShareSkillsPath()))
                .knowledges(resolveKnowledge(ctx))
                .build();
    }

    private ToolExecutionContext resolveToolExecutionContext(ReActAgentContext ctx) {
        return ToolExecutionContext.builder()
                .register(AGENT_CONTEXT_KEY, ctx)
                .register(THREAD_CONTEXT_KEY, tenantContextGetter.findTenantThreadContext().orElse(null))
                .build();
    }

    private List<Knowledge> resolveKnowledge(ReActAgentContext ctx) {
        List<Knowledge> knowledgeList = new ArrayList<>();
        List<String> knowledgeIds = ctx.getKnowledgeIds();
        if (knowledgeIds != null && !knowledgeIds.isEmpty()) {
            for (String knowledgeId : knowledgeIds) {
                knowledgeList.add(new AgentScopeKnowledge(ragVectorSearchPort, etlDocumentChunkStorePort, knowledgeId));
            }
        }
        return knowledgeList;
    }

    private StudioMessageHook resolveStudioMessageHook(AgentScopeReActAgentConfig config, ReActAgentContext ctx) {
        StudioConfig build = StudioConfig.builder()
                .project(config.getAgent().getName())
                .runName(config.getAgent().getAgentCode())
                .runId(ctx.getSessionId())
                .build();
        StudioClient studioClient = new StudioClient(build, agentStudioMessageHandler);
        studioClient.registerRun();
        return new StudioMessageHook(studioClient);
    }

    private Toolkit resolveToolkit(ReActAgentContext ctx) {
        Set<ToolCallback> tools = new HashSet<>();
        tools.addAll(agentToolsFactory.getToolCallbacks(MCP_TOOL, ctx.getTools()));
        tools.addAll(agentToolsFactory.getToolCallbacks(SYSTEM_TOOL, ctx.getTools()));
        return toolConverter.convertToToolkit(tools);
    }

}
