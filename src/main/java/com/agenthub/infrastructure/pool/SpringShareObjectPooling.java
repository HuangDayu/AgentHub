package com.agenthub.infrastructure.pool;

import com.agenthub.common.exception.NotFoundException;
import com.agenthub.application.port.out.repositories.KnowledgeBaseRepository;
import com.agenthub.application.port.out.repositories.ModelStrategyRepository;
import com.agenthub.domain.model.KnowledgeBase;
import com.agenthub.domain.model.SessionMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class SpringShareObjectPooling {
    private final SpringVectorStorePooling springVectorStorePooling;
    private final SpringChatModelPooling springChatModelPooling;
    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final ModelStrategyRepository modelStrategyRepository;
    private final JdbcChatMemoryRepository jdbcChatMemoryRepository;
    public static final Map<String, ChatClient> CHAT_CLIENT_MAP = new ConcurrentHashMap<>();

    public VectorStore getVectorStoreByKbId(String kbId) {
        return getVectorStoreByKb(getKnowledgeBase(kbId));
    }

    public VectorStore getVectorStoreByKb(KnowledgeBase knowledgeBase) {
        EmbeddingModel embeddingModel = springChatModelPooling.getOrCreateEmbeddingModel(knowledgeBase.embeddingModelConfigId());
        return getVectorStoreByConfigId(knowledgeBase.vectorStoreConfigId(), embeddingModel);
    }

    public VectorStore getVectorStoreByConfigId(String vectorStoreConfigId, EmbeddingModel embeddingModel) {
        VectorStore vectorStore = springVectorStorePooling.getOrCreate(vectorStoreConfigId, embeddingModel);
        if (vectorStore == null) throw new NotFoundException("VectorStore not found");
        return vectorStore;
    }

    public ChatClient getChatClientBySessionId(SessionMessage sessionMessage) {
        return CHAT_CLIENT_MAP.computeIfAbsent(sessionMessage.sessionId(), key -> {
            ChatModel chatModel = getChatModelByConfigId(sessionMessage.chatModelConfigId());
            MessageWindowChatMemory memory = MessageWindowChatMemory.builder()
                    .chatMemoryRepository(jdbcChatMemoryRepository)
                    .maxMessages(sessionMessage.strategy().getMaxMessages()).build();
            return ChatClient.builder(chatModel)
                    .defaultAdvisors(MessageChatMemoryAdvisor.builder(memory).build()).build();
        });
    }

    public ChatModel getChatModelByKbId(String kbId) {
        return getChatModelByKb(getKnowledgeBase(kbId));
    }

    public ChatModel getChatModelByKb(KnowledgeBase knowledgeBase) {
        return getChatModelByConfigId(knowledgeBase.chatModelConfigId());
    }

    public ChatModel getChatModelByConfigId(String chatModelConfigId) {
        ChatModel chatModel = springChatModelPooling.getOrCreateChatModel(chatModelConfigId);
        if (chatModel == null) throw new NotFoundException("ChatModel not found");
        return chatModel;
    }

    public EmbeddingModel getEmbeddingModelByKbId(String kbId) {
        return getEmbeddingModelByKb(getKnowledgeBase(kbId));
    }

    public EmbeddingModel getEmbeddingModelByKb(KnowledgeBase knowledgeBase) {
        return getEmbeddingModelByConfigId(knowledgeBase.embeddingModelConfigId());
    }

    public EmbeddingModel getEmbeddingModelByConfigId(String embeddingModelConfigId) {
        EmbeddingModel embeddingModel = springChatModelPooling.getOrCreateEmbeddingModel(embeddingModelConfigId);
        if (embeddingModel == null) throw new NotFoundException("EmbeddingModel not found");
        return embeddingModel;
    }

    private KnowledgeBase getKnowledgeBase(String kbId) {
        return knowledgeBaseRepository.findById(kbId)
                .orElseThrow(() -> new NotFoundException("KnowledgeBase not found"));
    }
}
