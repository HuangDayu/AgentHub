package com.agenthub.infrastructure.agents.subagent;

import cn.hutool.core.map.multi.RowKeyTable;
import cn.hutool.core.map.multi.Table;
import com.agenthub.application.command.SubAgentChatCommand;
import com.agenthub.application.factory.ReActAgentFactory;
import com.agenthub.application.port.out.agent.SubagentExecutionPort;
import com.agenthub.application.port.out.repositories.SubagentRepository;
import com.agenthub.application.port.out.repositories.SubsessionRepository;
import com.agenthub.application.usecase.ChatAttachmentUseCase;
import com.agenthub.domain.exception.NotFoundException;
import com.agenthub.domain.model.agent.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
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
    private final AgentStreamExecutor agentStreamExecutor;
    private final ExecutorService ttlExecutorService;
    private final ChatAttachmentUseCase chatAttachmentUseCase;
    private static final Table<String, String, SubagentEngineContext> RUNNING_TASKS = new RowKeyTable<>(new ConcurrentHashMap<>(), ConcurrentHashMap::new);


    public Flux<AgentMessage> executeSubagent(Subagent subagent, Subsession subsession, ReActAgentContext ctx, String input) {
        SubagentEngineContext subagentEngineContext = RUNNING_TASKS.get(subsession.getId(), subagent.getId());
        if (subagentEngineContext != null && subagentEngineContext.getStatus() == RUNNING) {
            return subagentEngineContext.getStreamFlux();
        }
        if (subagentEngineContext == null) {
            AbstractReActAgent agent = reActAgentFactory.create(ctx);
            subagentEngineContext = new SubagentEngineContext(subagent, subsession, ctx, agent, null, null);
            RUNNING_TASKS.put(subsession.getId(), subagent.getId(), subagentEngineContext);
        }
        ttlExecutorService.execute(new SubAgentRunnable(agentStreamExecutor, subagentRepository, subagentEngineContext, ttlExecutorService, input));
        return subagentEngineContext.getStreamFlux();

    }


    @Override
    public Flux<AgentMessage> stream(SubAgentChatCommand subAgentChatCommand) {
        SubagentEngineContext subagentEngineContext = RUNNING_TASKS.get(subAgentChatCommand.getSubSessionId(), subAgentChatCommand.getSubAgentId());
        if (subagentEngineContext == null) {
            throw new NotFoundException("Subagent not found: " + subAgentChatCommand.getSubAgentId());
        }
        if (subagentEngineContext.getStatus() == SubAgentStatus.RUNNING) {
            return subagentEngineContext.getStreamFlux();
        }
        AbstractReActAgent agent = subagentEngineContext.getAgent();
        return agent.streamMessages(buildMessages(subAgentChatCommand));
    }


    public void stop(Subagent subagent, String subsessionId) {
        RUNNING_TASKS.remove(subsessionId, subagent.getId());
    }

    private ReActAgentContext buildContext(Subagent subagent, Subsession subsession) {
        return ReActAgentContext.builder()
                .agent(mapToAgent(subagent))
                .sessionId(subsession != null ? subsession.getId() : subagent.getId())
                .chatModelId(subagent.getModelConfigId())
                .systemPrompt(subagent.getSystemPrompt())
                .build();
    }

    private List<AgentMessage> buildMessages(SubAgentChatCommand subAgentChatCommand) {
        List<AgentMessage> messages = new ArrayList<>();
        messages.add(new AgentMessage(AgentMessage.MessageType.USER, subAgentChatCommand.getUserMessage()));
        if (subAgentChatCommand.getFilePaths() != null && !subAgentChatCommand.getFilePaths().isEmpty()) {
            String messageWithContext = chatAttachmentUseCase.readFilesContent(subAgentChatCommand.getFilePaths());
            if (messageWithContext != null && !messageWithContext.isEmpty()) {
                messages.add(new AgentMessage(AgentMessage.MessageType.ASSISTANT, messageWithContext));
            }
        }
        return messages;
    }

    private com.agenthub.domain.model.agent.Agent mapToAgent(Subagent subagent) {
        com.agenthub.domain.model.agent.Agent agent = new com.agenthub.domain.model.agent.Agent();
        agent.setId(subagent.getId());
        agent.setName(subagent.getName());
        agent.setDescription(subagent.getDescription());
        agent.setTenantId(subagent.getTenantId());
        agent.setWorkspaceId(subagent.getWorkspaceId());
        return agent;
    }

    private class SubAgentRunnable implements Runnable {
        private final AgentStreamExecutor agentStreamExecutor;
        private final SubagentRepository subagentRepository;
        private final SubagentEngineContext subagentEngineContext;
        private final ExecutorService ttlExecutorService;
        private final String input;

        SubAgentRunnable(AgentStreamExecutor ase, SubagentRepository sr,
                         SubagentEngineContext ctx, ExecutorService es, String inp) {
            this.agentStreamExecutor = ase;
            this.subagentRepository = sr;
            this.subagentEngineContext = ctx;
            this.ttlExecutorService = es;
            this.input = inp;
        }

        @Override
        public void run() {
            Subagent subagent = subagentEngineContext.getSubagent();
            try {
                Flux<AgentMessage> flux = agentStreamExecutor.streamMessages(
                                subagentEngineContext.getAgent(), subagentEngineContext.getSubsession().getId(), input)
                        .doOnNext(msg -> tryComplete(subagent, RUNNING))
                        .doOnComplete(() -> tryComplete(subagent, COMPLETED))
                        .doOnError(e -> tryComplete(subagent, FAILED))
                        .subscribeOn(Schedulers.fromExecutorService(ttlExecutorService));
                subagentEngineContext.setStreamFlux(flux);
            } catch (Exception e) {
                log.error("Subagent {} failed", subagent.getId(), e);
                tryComplete(subagent, FAILED);
            }
        }

        private void tryComplete(Subagent subagent, SubAgentStatus status) {
            subagent.setStatus(status.name());
            subagentEngineContext.setStatus(status);
            subagentRepository.save(subagent);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubagentEngineContext {
        private Subagent subagent;
        private Subsession subsession;
        private ReActAgentContext context;
        private AbstractReActAgent agent;
        private Flux<AgentMessage> streamFlux;
        private SubAgentStatus status;
    }


}
