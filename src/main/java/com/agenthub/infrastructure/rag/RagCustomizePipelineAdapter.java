package com.agenthub.infrastructure.rag;

import com.agenthub.application.command.RagCommand;
import com.agenthub.application.port.out.rag.RagModelChatPort;
import com.agenthub.application.port.out.rag.RagRetrievalPort;
import com.agenthub.application.port.out.rag.RetrievalAugmentedGenerationPort;
import com.agenthub.application.port.out.repositories.AgentConfigRepository;
import com.agenthub.domain.model.agent.AgentMessage;
import com.agenthub.domain.model.agent.ChatMessage;
import com.agenthub.domain.model.rag.RetrievalChunk;
import com.agenthub.domain.model.rag.RagChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

import static com.agenthub.domain.enums.AgentConfigCategory.MODEL;
import static com.agenthub.domain.enums.AgentConfigType.CHAT_MODEL;

/**
 * @author huangdayu
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "agenthub.rag.type", havingValue = "customize")
public class RagCustomizePipelineAdapter implements RetrievalAugmentedGenerationPort {
    private final RagRetrievalPort ragRetrievalPort;
    private final RagModelChatPort ragModelChatPort;
    private final RagContextPromptBuilder ragContextPromptBuilder;
    private final AgentConfigRepository agentConfigRepository;


    @Override
    public List<RetrievalChunk> ragRetrieve(RagCommand ragCommand) {
        return ragRetrievalPort.retrieve(ragCommand);
    }

    @Override
    public AgentMessage ragChat(RagCommand ragCommand) {
        return ragModelChatPort.chat(buildSessionMessage(ragCommand));
    }

    @Override
    public Flux<AgentMessage> ragStream(RagCommand ragCommand) {
        return ragModelChatPort.stream(buildSessionMessage(ragCommand));
    }

    private RagChatMessage buildSessionMessage(RagCommand ragCommand) {
        String modelId = agentConfigRepository.getConfigId(ragCommand.getAgentId(), MODEL, CHAT_MODEL);
        List<ChatMessage> chatMessages = buildMessages(ragCommand);
        return new RagChatMessage(ragCommand.getSessionId(), ragCommand.getAgentId(), modelId, ragCommand.getModelStrategy(), chatMessages);
    }

    public List<ChatMessage> buildMessages(RagCommand ragCommand) {
        List<RetrievalChunk> contexts = ragRetrievalPort.retrieve(ragCommand);
        return ragContextPromptBuilder.build(new RagContextPromptBuilder.BuildInput(
                ragCommand.getSessionId(), ragCommand.getPromptTemplate(), ragCommand.getPrompt(), contexts));
    }


}
