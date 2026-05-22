package com.agenthub.infrastructure.agents.aliyun.model;

import com.agenthub.domain.enums.ModelSupplier;
import com.agenthub.domain.model.ModelConfig;
import io.agentscope.core.model.Model;
import io.agentscope.core.model.OllamaChatModel;
import org.springframework.stereotype.Component;

/**
 * Ollama AgentScope 原生模型工厂。
 * <p>
 * 使用 AgentScope 的 {@link OllamaChatModel} 创建本地 Ollama 模型实例。
 */
@Component
public class OllamaAgentScopeModelFactory implements AgentScopeModelFactory {

    @Override
    public ModelSupplier getSupplier() {
        return ModelSupplier.OLLAMA;
    }

    @Override
    public Model create(ModelConfig config) {
        return OllamaChatModel.builder()
                .modelName(config.getModel())
                .baseUrl(config.getBaseUrl())
                .build();
    }

}
