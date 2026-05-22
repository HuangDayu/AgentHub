package com.agenthub.infrastructure.agents.aliyun.model;

import com.agenthub.application.dto.ModelTestOutput;
import com.agenthub.application.port.out.ModelPoolManagerPort;
import com.agenthub.application.port.out.repositories.ModelConfigRepository;
import com.agenthub.domain.model.ModelConfig;
import io.agentscope.core.message.ContentBlock;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.model.GenerateOptions;
import io.agentscope.core.model.Model;
import io.agentscope.core.model.ChatUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * AgentScope 原生模型工厂注册表。
 * <p>
 * 自动发现所有 {@link AgentScopeModelFactory} Bean，按供应商注册，
 * 提供根据 configId 创建并缓存 AgentScope {@link Model} 实例的能力。
 * 实现 {@link ModelPoolManagerPort} 以支持缓存管理与模型测试。
 */
@Primary
@Component
public class AgentScopeModelFactoryRegistry implements ModelPoolManagerPort {

    private static final Logger log = LoggerFactory.getLogger(AgentScopeModelFactoryRegistry.class);

    private final Map<String, Model> modelCache = new ConcurrentHashMap<>();
    private final Map<com.agenthub.domain.enums.ModelSupplier, AgentScopeModelFactory> factoriesBySupplier;
    private final ModelConfigRepository modelConfigRepository;

    public AgentScopeModelFactoryRegistry(
            List<AgentScopeModelFactory> factories,
            ModelConfigRepository modelConfigRepository) {
        this.factoriesBySupplier = factories.stream()
                .collect(Collectors.toMap(
                        AgentScopeModelFactory::getSupplier, Function.identity()));
        this.modelConfigRepository = modelConfigRepository;
        log.info("Registered AgentScope model factories: {}", factoriesBySupplier.keySet());
    }

    /**
     * 根据 configId 获取或创建并缓存 AgentScope Model 实例。
     */
    public Model getOrCreateModel(String configId) {
        return modelCache.computeIfAbsent(configId, this::createModel);
    }

    private Model createModel(String configId) {
        ModelConfig config = modelConfigRepository.findById(configId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Model config not found: id=" + configId));
        AgentScopeModelFactory factory = factoriesBySupplier.get(config.getSupplier());
        if (factory == null) {
            throw new IllegalArgumentException("No AgentScope model factory for supplier: "
                    + config.getSupplier());
        }
        return factory.create(config);
    }

    @Override
    public void evictCache(String configId) {
        modelCache.remove(configId);
    }

    @Override
    public void evictAllCache() {
        modelCache.clear();
    }

    @Override
    public ModelTestOutput testModel(String configId) {
        ModelConfig config = modelConfigRepository.findById(configId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Model config not found: id=" + configId));
        try {
            Model model = getOrCreateModel(configId);
            var result = model.stream(
                    List.of(Msg.builder()
                            .role(MsgRole.USER)
                            .textContent("Hello")
                            .build()),
                    null,
                    GenerateOptions.builder().maxTokens(50).build())
                    .blockFirst();
            String text = "no response";
            if (result != null && !result.getContent().isEmpty()) {
                ContentBlock block = result.getContent().get(0);
                if (block instanceof TextBlock tb) {
                    text = tb.getText();
                }
            }
            String truncated = text.substring(0, Math.min(50, text.length()));
            return new ModelTestOutput(true, "AgentScope model test successful",
                    "供应商: " + config.getSupplier() + ", 模型: " + config.getModel()
                            + ", 响应: " + truncated);
        } catch (Exception e) {
            log.error("AgentScope model test failed for config {}: {}", configId, e.getMessage());
            return new ModelTestOutput(false, "测试失败: " + e.getMessage(),
                    e.getClass().getSimpleName());
        }
    }

}
