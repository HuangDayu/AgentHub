package com.agenthub.infrastructure.agents.aliyun.model;

import com.agenthub.domain.enums.ModelSupplier;
import com.agenthub.domain.model.ModelConfig;
import io.agentscope.core.formatter.openai.DeepSeekFormatter;
import io.agentscope.core.model.Model;
import io.agentscope.core.model.OpenAIChatModel;
import org.springframework.stereotype.Component;

/**
 * DeepSeek AgentScope 原生模型工厂。
 * <p>
 * 使用 AgentScope 的 {@link OpenAIChatModel} + {@link DeepSeekFormatter}
 * 创建 DeepSeek 模型实例，处理 DeepSeek API 的特殊要求（无 name 字段等）。
 */
@Component
public class DeepSeekAgentScopeModelFactory implements AgentScopeModelFactory {

    @Override
    public ModelSupplier getSupplier() {
        return ModelSupplier.DEEPSEEK;
    }

    @Override
    public Model create(ModelConfig config) {
        return OpenAIChatModel.builder()
                .modelName(config.getModel())
                .apiKey(config.getApiKey())
                .baseUrl(config.getBaseUrl())
                .stream(true)
                .formatter(new DeepSeekFormatter())
                .build();
    }

}
