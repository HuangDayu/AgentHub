package com.agenthub.infrastructure.agents.aliyun.model;

import com.agenthub.domain.enums.ModelSupplier;
import com.agenthub.domain.model.ModelConfig;
import io.agentscope.core.model.Model;

/**
 * AgentScope 原生模型工厂接口。
 * <p>
 * 根据 {@link ModelConfig} 创建 AgentScope 框架的 {@link Model} 实例，
 * 用于 {@code agentscope-harness} 中 {@code HarnessAgent.builder().model()} 调用。
 * 与 {@link com.agenthub.infrastructure.model.ModelFactory} 并列，
 * 后者创建 Spring AI 的 {@code ChatModel} 实例。
 */
public interface AgentScopeModelFactory {

    /**
     * 获取当前工厂支持的供应商。
     */
    ModelSupplier getSupplier();

    /**
     * 根据配置创建 AgentScope Model 实例。
     *
     * @param config 模型配置
     * @return AgentScope Model 实例
     */
    Model create(ModelConfig config);

}
