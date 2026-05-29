package com.agenthub.infrastructure.agents.subagent;

import cn.hutool.core.map.multi.RowKeyTable;
import cn.hutool.core.map.multi.Table;
import com.agenthub.application.command.SubAgentChatCommand;
import com.agenthub.application.command.SubagentExecutionCommand;
import com.agenthub.application.factory.AgentContextFactory;
import com.agenthub.application.factory.ReActAgentFactory;
import com.agenthub.application.port.out.agent.SubagentExecutionPort;
import com.agenthub.application.port.out.repositories.SubagentRepository;
import com.agenthub.application.port.out.repositories.SubsessionRepository;
import com.agenthub.domain.exception.NotFoundException;
import com.agenthub.domain.model.agent.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import static com.agenthub.infrastructure.agents.subagent.SubAgentStatus.*;

/**
 * 子Agent引擎，执行子Agent对话并将消息保存到ChatMessage。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubagentEngine implements SubagentExecutionPort {

    private final SubagentRepository subagentRepository;
    private final SubsessionRepository subsessionRepository;
    private final ReActAgentFactory reActAgentFactory;
    private final AgentContextFactory agentContextFactory;
    private final AgentStreamExecutor agentStreamExecutor;
    private final ExecutorService ttlExecutorService;
    private static final Table<String, String, SubagentEngineContext> RUNNING_TASKS = new RowKeyTable<>(new ConcurrentHashMap<>(), ConcurrentHashMap::new);

    @Override
    public void execute(SubagentExecutionCommand command) {
        SubagentEngineContext running = runningContext(command);
        if (running != null && running.getStatus() == RUNNING) return;
        SubagentEngineContext context = buildEngineContext(command);
        RUNNING_TASKS.put(command.getSubsession().getId(), command.getSubagent().getId(), context);
        start(context, command.getInput());
    }

    @Override
    public Flux<AgentMessage> stream(SubAgentChatCommand command) {
        Subsession subsession = findSubsession(command.getSubSessionId());
        Subagent subagent = findSubagent(subsession.getSubagentId());
        SubagentEngineContext context = getOrCreateContext(subagent, subsession);
        if (context.getStatus() == RUNNING && context.getStreamFlux() != null) return context.getStreamFlux();
        return agentStreamExecutor.streamMessages(streamCommand(context, command));
    }

    @Override
    public boolean stop(Subagent subagent, String subsessionId) {
        SubagentEngineContext context = RUNNING_TASKS.remove(subsessionId, subagent.getId());
        if (context == null) return false;
        context.getAgent().interrupt();
        context.setStatus(INTERRUPTED);
        if (context.getDisposable() != null) context.getDisposable().dispose();
        return true;
    }

    private SubagentEngineContext runningContext(SubagentExecutionCommand command) {
        return RUNNING_TASKS.get(command.getSubsession().getId(), command.getSubagent().getId());
    }

    private SubagentEngineContext buildEngineContext(SubagentExecutionCommand command) {
        AbstractReActAgent agent = reActAgentFactory.create(command.getContext());
        SubagentEngineContext context = new SubagentEngineContext();
        fillEngineContext(command, context, agent);
        return context;
    }

    private void fillEngineContext(SubagentExecutionCommand command,
                                   SubagentEngineContext context,
                                   AbstractReActAgent agent) {
        context.setSubagent(command.getSubagent());
        context.setSubsession(command.getSubsession());
        context.setContext(command.getContext());
        context.setAgent(agent);
        context.setStatus(RUNNING);
    }

    private void start(SubagentEngineContext context, String input) {
        markStatus(context, RUNNING);
        Flux<AgentMessage> flux = buildExecutionFlux(context, input).cache(0);
        context.setStreamFlux(flux);
        context.setDisposable(flux.subscribe());
    }

    private Flux<AgentMessage> buildExecutionFlux(SubagentEngineContext context, String input) {
        return agentStreamExecutor.streamMessages(streamCommand(context, input))
                .subscribeOn(Schedulers.fromExecutorService(ttlExecutorService))
                .doOnComplete(() -> markStatus(context, COMPLETED))
                .doOnError(e -> markFailed(context, e))
                .doFinally(signal -> RUNNING_TASKS.remove(context.getSubsession().getId(), context.getSubagent().getId()));
    }

    private AgentStreamCommand streamCommand(SubagentEngineContext context, SubAgentChatCommand command) {
        AgentStreamCommand streamCommand = streamCommand(context, command.getUserMessage());
        streamCommand.setFilePaths(command.getFilePaths());
        return streamCommand;
    }

    private AgentStreamCommand streamCommand(SubagentEngineContext context, String input) {
        AgentStreamCommand command = new AgentStreamCommand();
        command.setAgent(context.getAgent());
        command.setSessionId(context.getSubsession().getId());
        command.setUserMessage(input);
        return command;
    }

    private SubagentEngineContext getOrCreateContext(Subagent subagent, Subsession subsession) {
        SubagentEngineContext context = RUNNING_TASKS.get(subsession.getId(), subagent.getId());
        if (context != null) return context;
        context = buildEngineContext(executionCommand(subagent, subsession));
        RUNNING_TASKS.put(subsession.getId(), subagent.getId(), context);
        return context;
    }

    private SubagentExecutionCommand executionCommand(Subagent subagent, Subsession subsession) {
        SubagentExecutionCommand command = new SubagentExecutionCommand();
        command.setSubagent(subagent);
        command.setSubsession(subsession);
        command.setContext(buildContext(subagent, subsession));
        return command;
    }

    private ReActAgentContext buildContext(Subagent subagent, Subsession subsession) {
        ReActAgentContext parent = agentContextFactory.buildContext(subagent.getParentAgentId(), subsession.getParentSessionId());
        return inheritContext(parent, subagent, subsession);
    }

    private ReActAgentContext inheritContext(ReActAgentContext parent, Subagent subagent, Subsession subsession) {
        ReActAgentContext.ReActAgentContextBuilder builder = baseContext(parent, subagent, subsession);
        return inheritCollections(builder, parent).build();
    }

    private ReActAgentContext.ReActAgentContextBuilder baseContext(ReActAgentContext parent, Subagent subagent, Subsession subsession) {
        return ReActAgentContext.builder().agent(mapToAgent(subagent))
                .sessionId(subsession.getId()).chatModelId(subagent.getModelConfigId())
                .systemPrompt(subagent.getSystemPrompt()).modelStrategy(parent.getModelStrategy())
                .toolStrategy(parent.getToolStrategy()).guardrailStrategy(parent.getGuardrailStrategy())
                .retrievalStrategy(parent.getRetrievalStrategy()).agentConfigs(parent.getAgentConfigs())
                .workspace(parent.getWorkspace());
    }

    private ReActAgentContext.ReActAgentContextBuilder inheritCollections(ReActAgentContext.ReActAgentContextBuilder builder, ReActAgentContext parent) {
        return builder.toolInfos(parent.getToolInfos() == null ? List.of() : parent.getToolInfos())
                .toolCallbacks(parent.getToolCallbacks() == null ? List.of() : parent.getToolCallbacks())
                .knowledgeIds(parent.getKnowledgeIds() == null ? List.of() : parent.getKnowledgeIds());
    }

    private Subsession findSubsession(String subsessionId) {
        return subsessionRepository.findById(subsessionId)
                .orElseThrow(() -> new NotFoundException("Subsession not found: " + subsessionId));
    }

    private Subagent findSubagent(String subagentId) {
        return subagentRepository.findById(subagentId)
                .orElseThrow(() -> new NotFoundException("Subagent not found: " + subagentId));
    }

    private void markFailed(SubagentEngineContext context, Throwable throwable) {
        log.error("Subagent {} failed", context.getSubagent().getId(), throwable);
        markStatus(context, FAILED);
    }

    private void markStatus(SubagentEngineContext context, SubAgentStatus status) {
        Subagent subagent = context.getSubagent();
        subagent.setStatus(status.name());
        context.setStatus(status);
        subagentRepository.save(subagent);
    }

    private Agent mapToAgent(Subagent subagent) {
        Agent agent = new Agent();
        agent.setId(subagent.getId());
        agent.setName(subagent.getName());
        agent.setDescription(subagent.getDescription());
        agent.setTenantId(subagent.getTenantId());
        agent.setWorkspaceId(subagent.getWorkspaceId());
        return agent;
    }

    @Data
    @NoArgsConstructor
    public static class SubagentEngineContext {
        private Subagent subagent;
        private Subsession subsession;
        private ReActAgentContext context;
        private AbstractReActAgent agent;
        private Flux<AgentMessage> streamFlux;
        private SubAgentStatus status;
        private Disposable disposable;
    }
}
