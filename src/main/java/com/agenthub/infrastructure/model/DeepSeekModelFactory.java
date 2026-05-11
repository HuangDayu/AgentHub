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
 * DeepSeek 模型工厂。
 * <p>
 * DeepSeek 使用 OpenAI 兼容协议，baseUrl 通常指向 DeepSeek 的 API endpoint，
 * 如 https://api.deepseek.com/v1。
 * </p>
 */
@Component
public class DeepSeekModelFactory implements ModelFactory {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekModelFactory.class);

    private static final String DEFAULT_BASE_URL = "https://api.deepseek.com/v1";

    @Override
    public String getSupplier() {
        return "DEEPSEEK";
    }

    @Override
    public ChatModel createChatModel(ModelConfig config) {
        log.info("Creating DeepSeek ChatModel: model={}", config.getModel());

        OpenAiApi openAiApi = createOpenAiApi(config);

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(config.getModel())
                .build();

        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(options)
                .build();
    }

    @Override
    public EmbeddingModel createEmbeddingModel(ModelConfig config) {
        log.info("Creating DeepSeek EmbeddingModel: model={}", config.getModel());

        OpenAiApi openAiApi = createOpenAiApi(config);

        OpenAiEmbeddingOptions options = OpenAiEmbeddingOptions.builder()
                .model(config.getModel())
                .build();

        return new OpenAiEmbeddingModel(openAiApi, MetadataMode.EMBED, options);
    }

    private OpenAiApi createOpenAiApi(ModelConfig config) {
        String baseUrl = (config.getBaseUrl() != null && !config.getBaseUrl().isBlank())
                ? config.getBaseUrl() : DEFAULT_BASE_URL;

        return OpenAiApi.builder()
                .apiKey(config.getApiKey())
                .baseUrl(baseUrl)
                .build();
    }
}