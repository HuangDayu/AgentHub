package com.agenthub.infrastructure.rag;

import com.agenthub.application.command.RagCommand;
import com.agenthub.application.port.out.rag.RetrievalAugmentedGenerationPort;
import com.agenthub.application.port.out.repositories.AgentConfigRepository;
import com.agenthub.domain.model.agent.AgentMessage;
import com.agenthub.domain.model.rag.RetrievalChunk;
import com.agenthub.domain.model.strategy.ModelStrategy;
import com.agenthub.infrastructure.converter.AgentMessageConverter;
import com.agenthub.infrastructure.factory.SpringShareObjectFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.tool.DefaultToolCallingChatOptions;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.join.ConcatenationDocumentJoiner;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.agenthub.domain.enums.AgentConfigCategory.MODEL;
import static com.agenthub.domain.enums.AgentConfigType.CHAT_MODEL;
import static com.agenthub.infrastructure.rag.RagContextPromptTemplater.*;

/**
 * @author huangdayu
 * @doc https://docs.spring.io/spring-ai/reference/2.0/api/retrieval-augmented-generation.html
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "agenthub.rag.type", havingValue = "spring", matchIfMissing = true)
public class RagSpringPipelineAdapter implements RetrievalAugmentedGenerationPort {

    private final SpringShareObjectFactory springShareObjectFactory;
    private final AgentConfigRepository agentConfigRepository;


    @Override
    public List<RetrievalChunk> ragRetrieve(RagCommand ragCommand) {
        List<RetrievalChunk> list = new ArrayList<>();
        for (String kbId : ragCommand.getKbIds()) {
            list.addAll(retrieveFromSingleKb(kbId, ragCommand));
        }
        return list;
    }

    private List<RetrievalChunk> retrieveFromSingleKb(String kbId, RagCommand ragCommand) {
        VectorStore vectorStore = springShareObjectFactory.getVectorStoreByKbId(kbId);
        DocumentRetriever documentRetriever = buildDocumentRetriever(vectorStore, ragCommand);
        List<Document> documents = documentRetriever.retrieve(new Query(ragCommand.getPrompt()));
        return documents.isEmpty() ? List.of() : convertResults(kbId, documents);
    }


    private List<RetrievalChunk> convertResults(String kbId, List<Document> results) {
        return results.stream()
                .map(d -> toResult(kbId, d))
                .filter(r -> r != null)
                .toList();
    }

    private RetrievalChunk toResult(String kbId, Document doc) {
        Map<String, Object> meta = doc.getMetadata();
        String docId = meta != null ? String.valueOf(meta.getOrDefault("document_id", "")) : "";
        String content = doc.getText() != null ? doc.getText() : "";
        String id = doc.getId() != null ? doc.getId() : docId;
        return new RetrievalChunk(content, null, docId, id, doc.getScore(), kbId);
    }

    @Override
    public AgentMessage ragChat(RagCommand ragCommand) {
        return ChatClient.builder(getAgentChatModel(ragCommand.getAgentId())).build()
                .prompt(ragCommand.getPrompt())
                .options(getToolChatOptions(ragCommand))
                .advisors(buildAdvisor(ragCommand))
                .call()
                .entity(AgentMessage.class);
    }

    @Override
    public Flux<AgentMessage> ragStream(RagCommand ragCommand) {
        return ChatClient.builder(getAgentChatModel(ragCommand.getAgentId())).build()
                .prompt(ragCommand.getPrompt())
                .options(getToolChatOptions(ragCommand))
                .advisors(buildAdvisor(ragCommand))
                .stream()
                .chatResponse()
                .map(v -> v.getResult() != null ? AgentMessageConverter.fromMessage(v.getResult().getOutput()) : new AgentMessage());
    }

    private ChatOptions getToolChatOptions(RagCommand ragCommand) {
        DefaultToolCallingChatOptions options = new DefaultToolCallingChatOptions();
        options.setTemperature(ragCommand.getModelStrategy().getTemperature());
        options.setTopK(ragCommand.getModelStrategy().getTopK());
        options.setTopP(ragCommand.getModelStrategy().getTopP());
        options.setMaxTokens(ragCommand.getModelStrategy().getMaxTokens());
        options.setInternalToolExecutionEnabled(true);
        options.setToolContext(Map.of(ragCommand.getAgentId(), ragCommand));
        return options;
    }

    private ChatModel getAgentChatModel(String agentId) {
        String modelId = agentConfigRepository.getConfigId(agentId, MODEL, CHAT_MODEL);
        return springShareObjectFactory.getChatModelByConfigId(modelId);
    }

    private List<Advisor> buildAdvisor(RagCommand ragCommand) {
        List<Advisor> advisors = new LinkedList<>();
        for (String kbId : ragCommand.getKbIds()) {
            ChatModel chatModel = springShareObjectFactory.getChatModelByKbId(kbId);
            VectorStore vectorStore = springShareObjectFactory.getVectorStoreByKbId(kbId);
            RetrievalAugmentationAdvisor advisor = RetrievalAugmentationAdvisor.builder()
                    // 预处理： 创建查询转换器,用于查询的压缩、重写、翻译
                    .queryTransformers(buildQueryTransformers(ragCommand, chatModel))
                    //检索： 创建向量存储文档检索器
                    .documentRetriever(buildDocumentRetriever(vectorStore, ragCommand))
                    // 后处理：文档连接器，用于连接检索到的文档
                    .documentJoiner(new ConcatenationDocumentJoiner())
                    // 生成： 上下文查询增强器，允许空上下文
                    .queryAugmenter(ContextualQueryAugmenter.builder().allowEmptyContext(true).build())
                    .build();
            advisors.add(advisor);
        }
        return advisors;
    }

    private DocumentRetriever buildDocumentRetriever(VectorStore vectorStore, RagCommand query) {
        return VectorStoreDocumentRetriever.builder()
                .similarityThreshold(query.getStrategy().getScoreThreshold())
                .topK(query.getStrategy().getTopK())
                .vectorStore(vectorStore)
                .build();
    }


    /**
     * 构建查询转换器 : 压缩、重写、翻译
     *
     * @param chatModel
     * @return
     */
    private List<QueryTransformer> buildQueryTransformers(RagCommand ragCommand, ChatModel chatModel) {
        ChatClient.Builder chatClientBuilder = ChatClient.builder(chatModel);
        List<QueryTransformer> queryTransformers = new LinkedList<>();
        if (ragCommand.getStrategy().isEnableQueryRewrite()) queryTransformers.add(buildRewriteTransformer(chatClientBuilder, ragCommand));
        if (ragCommand.getStrategy().isEnableTranslationQuery()) queryTransformers.add(buildTranslationTransformer(chatClientBuilder));
        if (ragCommand.getStrategy().isEnableCompressionQuery()) queryTransformers.add(buildCompressionTransformer(chatClientBuilder));
        return queryTransformers;
    }

    private QueryTransformer buildRewriteTransformer(ChatClient.Builder builder, RagCommand cmd) {
        return RewriteQueryTransformer.builder().chatClientBuilder(builder).promptTemplate(new PromptTemplate(REWRITE_PROMPT_TEMPLATE.formatted(cmd.getPrompt()))).build();
    }

    private QueryTransformer buildTranslationTransformer(ChatClient.Builder builder) {
        return TranslationQueryTransformer.builder().chatClientBuilder(builder).promptTemplate(new PromptTemplate(TRANSLATION_PROMPT_TEMPLATE)).targetLanguage("Chinese").build();
    }

    private QueryTransformer buildCompressionTransformer(ChatClient.Builder builder) {
        return CompressionQueryTransformer.builder().chatClientBuilder(builder).promptTemplate(new PromptTemplate(COMPRESSION_PROMPT_TEMPLATE)).build();
    }

}
