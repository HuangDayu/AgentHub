package com.agenthub.infrastructure.rag;

import com.agenthub.application.command.RagCommand;
import com.agenthub.application.port.out.rag.RetrievalAugmentedGenerationPort;
import com.agenthub.application.port.out.repositories.AgentConfigRepository;
import com.agenthub.application.port.out.repositories.FunctionToolsRepository;
import com.agenthub.domain.model.FunctionTool;
import com.agenthub.domain.model.ModelStrategy;
import com.agenthub.infrastructure.pool.SpringShareObjectPooling;
import com.agenthub.infrastructure.tools.function_tools.FunctionToolsFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.model.tool.DefaultToolCallingChatOptions;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.join.ConcatenationDocumentJoiner;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.agenthub.domain.model.AgentConfig.Category.MODEL;
import static com.agenthub.domain.model.AgentConfig.Type.CHAT_MODEL;
import static com.agenthub.infrastructure.rag.RagPromptTemplate.*;

/**
 * @author huangdayu
 * @doc https://docs.spring.io/spring-ai/reference/2.0/api/retrieval-augmented-generation.html
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "agenthub.rag.impl-type", havingValue = "spring", matchIfMissing = true)
public class RagSpringPipelineAdapter implements RetrievalAugmentedGenerationPort {

    private final SpringShareObjectPooling springShareObjectPooling;
    private final AgentConfigRepository agentConfigRepository;
    private final FunctionToolsRepository functionToolsRepository;
    private final FunctionToolsFactory functionToolsFactory;


    @Override
    public String ragChat(RagCommand ragCommand) {
        return ChatClient.builder(getAgentChatModel(ragCommand.agentId())).build()
                .prompt(ragCommand.prompt())
                .options(getToolChatOptions(ragCommand))
                .advisors(buildAdvisor(ragCommand))
                .call()
                .content();
    }

    @Override
    public Flux<String> ragStream(RagCommand ragCommand) {
        return ChatClient.builder(getAgentChatModel(ragCommand.agentId())).build()
                .prompt(ragCommand.prompt())
                .options(getToolChatOptions(ragCommand))
                .advisors(buildAdvisor(ragCommand))
                .stream()
                .content();
    }

    private ChatOptions getToolChatOptions(RagCommand ragCommand) {
        ModelStrategy modelStrategy = ragCommand.modelStrategy();
        DefaultToolCallingChatOptions options = new DefaultToolCallingChatOptions();
        options.setTemperature(modelStrategy.getTemperature());
        options.setTopK(modelStrategy.getTopK());
        options.setTopP(modelStrategy.getTopP());
        options.setMaxTokens(modelStrategy.getMaxTokens());
        options.setInternalToolExecutionEnabled(true);
        options.setToolCallbacks(getFunctionTools());
        options.setToolContext(Map.of(ragCommand.agentId(), ragCommand));
        return options;
    }

    private List<ToolCallback> getFunctionTools() {
        Set<String> functionSet = functionToolsRepository.findByEnabled(true).stream()
                .map(FunctionTool::getToolClassName).collect(Collectors.toSet());
        return functionToolsFactory.getToolCallbacks().stream()
                .filter(toolCallback -> functionSet.contains(toolCallback.getClass().getName()))
                .collect(Collectors.toList());
    }

    private ChatModel getAgentChatModel(String agentId) {
        String modelId = agentConfigRepository.getConfigId(agentId, MODEL, CHAT_MODEL);
        return springShareObjectPooling.getChatModelByConfigId(modelId);
    }

    private List<Advisor> buildAdvisor(RagCommand ragCommand) {
        List<Advisor> advisors = new LinkedList<>();
        for (String kbId : ragCommand.kbIds()) {
            ChatModel chatModel = springShareObjectPooling.getChatModelByKbId(kbId);
            VectorStore vectorStore = springShareObjectPooling.getVectorStoreByKbId(kbId);
            RetrievalAugmentationAdvisor advisor = RetrievalAugmentationAdvisor.builder()
                    .queryTransformers(buildQueryTransformers(ragCommand, chatModel)) // 创建查询转换器,用于查询的压缩、重写、翻译
                    .documentRetriever(buildDocumentRetriever(vectorStore, ragCommand)) // 创建向量存储文档检索器
                    .documentJoiner(new ConcatenationDocumentJoiner()) // 文档连接器，用于连接检索到的文档
                    .queryAugmenter(ContextualQueryAugmenter.builder().allowEmptyContext(true).build()) // 上下文查询增强器，允许空上下文
                    .build();
            advisors.add(advisor);
        }
        return advisors;
    }

    private DocumentRetriever buildDocumentRetriever(VectorStore vectorStore, RagCommand query) {
        return VectorStoreDocumentRetriever.builder()
                .similarityThreshold(query.strategy().getScoreThreshold())
                .topK(query.strategy().getTopK())
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
        if (ragCommand.strategy().isEnableQueryRewrite()) {
            queryTransformers.add(RewriteQueryTransformer.builder().chatClientBuilder(chatClientBuilder).promptTemplate(new PromptTemplate(REWRITE_PROMPT_TEMPLATE.formatted(ragCommand.prompt()))).build());
        }
        if (ragCommand.strategy().isEnableTranslationQuery()) {
            queryTransformers.add(TranslationQueryTransformer.builder().chatClientBuilder(chatClientBuilder).promptTemplate(new PromptTemplate(TRANSLATION_PROMPT_TEMPLATE)).targetLanguage("Chinese").build());
        }
        if (ragCommand.strategy().isEnableCompressionQuery()) {
            queryTransformers.add(CompressionQueryTransformer.builder().chatClientBuilder(chatClientBuilder).promptTemplate(new PromptTemplate(COMPRESSION_PROMPT_TEMPLATE)).build());
        }
        return queryTransformers;
    }

}
