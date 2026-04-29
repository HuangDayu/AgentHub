package com.agenthub.infrastructure.spring.model;

import com.agenthub.domain.model.ModelConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;

/**
 * OpenRouter 模型工厂。
 * <p>
 * OpenRouter 使用 OpenAI 兼容协议，因此复用 OpenAI API Client，
 * baseUrl 指向 https://openrouter.ai/api/v1。
 * </p>
 */
@Component
public class OpenRouterModelFactory implements ModelFactory {

    private static final Logger log = LoggerFactory.getLogger(OpenRouterModelFactory.class);

    private static final String DEFAULT_BASE_URL = "https://openrouter.ai/api/v1";

    @Override
    public String getSupplier() {
        return "OPENROUTER";
    }

    @Override
    public ChatModel createChatModel(ModelConfig config) {
        log.info("Creating OpenRouter ChatModel: model={}", config.model());

        OpenAiApi openAiApi = createOpenAiApi(config);

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(config.model())
                .build();

        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(options)
                .build();
    }

    @Override
    public EmbeddingModel createEmbeddingModel(ModelConfig config) {
        log.info("Creating OpenRouter EmbeddingModel: model={}", config.model());

        OpenAiApi openAiApi = createOpenAiApi(config);

        OpenAiEmbeddingOptions options = OpenAiEmbeddingOptions.builder()
                .model(config.model())
                .build();

        return new OpenAiEmbeddingModel(openAiApi, MetadataMode.EMBED, options);
    }

    private OpenAiApi createOpenAiApi(ModelConfig config) {
        String baseUrl = (config.baseUrl() != null && !config.baseUrl().isBlank())
                ? config.baseUrl() : DEFAULT_BASE_URL;

        return OpenAiApi.builder()
                .apiKey(config.apiKey())
                .baseUrl(baseUrl)
                .build();
    }
}