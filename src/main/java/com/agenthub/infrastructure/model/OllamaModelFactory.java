package com.agenthub.infrastructure.model;

import com.agenthub.domain.enums.ModelSupplier;
import com.agenthub.domain.model.ModelConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.ollama.api.OllamaEmbeddingOptions;
import org.springframework.stereotype.Component;

/**
 * Ollama 模型工厂，基于 Spring AI 的 OllamaChatModel.Builder 创建模型。
 */
@Component
public class OllamaModelFactory implements ModelFactory {

    private static final Logger log = LoggerFactory.getLogger(OllamaModelFactory.class);

    @Override
    public ModelSupplier getSupplier() {
        return ModelSupplier.OLLAMA;
    }

    @Override
    public ChatModel createChatModel(ModelConfig config) {
        log.info("Creating Ollama ChatModel: model={}, baseUrl={}", config.getModel(), config.getBaseUrl());

        OllamaApi ollamaApi = createOllamaApi(config);

        OllamaChatOptions options = OllamaChatOptions.builder()
                .model(config.getModel())
                .build();

        return OllamaChatModel.builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(options)
                .build();
    }

    @Override
    public EmbeddingModel createEmbeddingModel(ModelConfig config) {
        log.info("Creating Ollama EmbeddingModel: model={}", config.getModel());

        OllamaApi ollamaApi = createOllamaApi(config);

        OllamaEmbeddingOptions options = OllamaEmbeddingOptions.builder()
                .model(config.getModel())
                .build();

        return OllamaEmbeddingModel.builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(options)
                .build();
    }

    private OllamaApi createOllamaApi(ModelConfig config) {
        String baseUrl = (config.getBaseUrl() != null && !config.getBaseUrl().isBlank())
                ? config.getBaseUrl() : "http://localhost:11434";
        return OllamaApi.builder()
                .baseUrl(baseUrl)
                .build();
    }
}