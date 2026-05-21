package com.agenthub.domain.model.workflow;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/**
 * 节点配置值对象。
 * 存储节点的特定配置信息，如LLM节点的模型配置等。
 *
 * @author huangdayu
 */
@Data
public class NodeConfig {

    /** 配置参数映射 */
    private final Map<String, Object> parameters;

    /** 超时时间（毫秒） */
    private final long timeoutMs;

    /** 重试次数 */
    private final int retryCount;

    /**
     * 创建节点配置。
     *
     * @param parameters 配置参数
     * @param timeoutMs 超时时间
     * @param retryCount 重试次数
     */
    @JsonCreator
    public NodeConfig(@JsonProperty("parameters") Map<String, Object> parameters,
                      @JsonProperty("timeoutMs") long timeoutMs,
                      @JsonProperty("retryCount") int retryCount) {
        this.parameters = parameters != null ? Map.copyOf(parameters) : Map.of();
        this.timeoutMs = timeoutMs;
        this.retryCount = retryCount;
    }

    /**
     * 创建默认配置。
     *
     * @return 默认配置
     */
    public static NodeConfig defaultConfig() {
        return new NodeConfig(Map.of(), 30000, 0);
    }

    /**
     * 获取指定参数值。
     *
     * @param key 参数键
     * @return 参数值
     */
    public Object getParameter(String key) {
        return parameters.get(key);
    }

    /**
     * 获取指定参数值并转换为指定类型。
     *
     * @param key 参数键
     * @param type 目标类型
     * @param <T> 泛型类型
     * @return 转换后的参数值
     */
    @SuppressWarnings("unchecked")
    public <T> T getParameter(String key, Class<T> type) {
        Object value = parameters.get(key);
        return value != null ? (T) value : null;
    }
}
