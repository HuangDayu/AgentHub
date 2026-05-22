package com.agenthub.infrastructure.agents.aliyun.model;

import com.agenthub.domain.enums.ModelSupplier;
import com.agenthub.domain.model.ModelConfig;
import io.agentscope.core.model.Model;
import io.agentscope.core.model.OpenAIChatModel;
import org.springframework.stereotype.Component;

/**
 * OpenAI AgentScope 原生模型工厂。
 * <p>
 * 使用 AgentScope 的 {@link OpenAIChatModel} 创建模型实例，
 * 支持标准的 OpenAI API 兼容格式。
 */
@Component
public class OpenAiAgentScopeModelFactory implements AgentScopeModelFactory {

    @Override
    public ModelSupplier getSupplier() {
        return ModelSupplier.OPENAI;
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
