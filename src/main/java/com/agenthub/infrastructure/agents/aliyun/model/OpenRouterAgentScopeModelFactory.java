package com.agenthub.infrastructure.agents.aliyun.model;

import com.agenthub.domain.enums.ModelSupplier;
import com.agenthub.domain.model.ModelConfig;
import io.agentscope.core.model.Model;
import io.agentscope.core.model.OpenAIChatModel;
import org.springframework.stereotype.Component;

/**
 * OpenRouter AgentScope 原生模型工厂。
 * <p>
 * OpenRouter 兼容 OpenAI API 格式，使用 {@link OpenAIChatModel} 创建模型实例。
 */
@Component
public class OpenRouterAgentScopeModelFactory implements AgentScopeModelFactory {

    @Override
    public ModelSupplier getSupplier() {
        return ModelSupplier.OPENROUTER;
    }

    @Override
    public Model create(ModelConfig config) {
        return OpenAIChatModel.builder()
                .modelName(config.getModel())
                .apiKey(config.getApiKey())
                .baseUrl(config.getBaseUrl())
                .stream(true)
                .build();
    }

}
