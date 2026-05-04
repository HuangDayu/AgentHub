package com.agenthub.infrastructure.model;

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
 * OpenAI 模型工厂，基于 Spring AI 的 OpenAiChatModel.Builder 创建模型。
 */
@Component
public class OpenAiModelFactory implements ModelFactory {

    private static final Logger log = LoggerFactory.getLogger(OpenAiModelFactory.class);

    @Override
    public String getSupplier() {
        return "OPENAI";
    }

    @Override
    public ChatModel createChatModel(ModelConfig config) {
        log.info("Creating OpenAI ChatModel: model={}, baseUrl={}", config.model(), config.baseUrl());

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
        log.info("Creating OpenAI EmbeddingModel: model={}", config.model());

        OpenAiApi openAiApi = createOpenAiApi(config);

        OpenAiEmbeddingOptions options = OpenAiEmbeddingOptions.builder()
                .model(config.model())
                .build();

        return new OpenAiEmbeddingModel(openAiApi, MetadataMode.EMBED, options);
    }

    private OpenAiApi createOpenAiApi(ModelConfig config) {
        OpenAiApi.Builder apiBuilder = OpenAiApi.builder()
                .apiKey(config.apiKey());

        if (config.baseUrl() != null && !config.baseUrl().isBlank()) {
            apiBuilder.baseUrl(config.baseUrl());
        }

        return apiBuilder.build();
    }
}