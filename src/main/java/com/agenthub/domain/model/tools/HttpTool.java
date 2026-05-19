package com.agenthub.domain.model.tools;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 工具领域模型。
 * <p>
 * 包含工具注册信息及 HTTP 调用所需的元数据。
 *
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HttpTool {
    /**
     * 工具唯一标识
     */
    private String id;
    /**
     * 工具名称
     */
    private String name;
    /**
     * 工具描述
     */
    private String description;
    /**
     * 是否启用
     */
    private boolean enabled;
    /**
     * HTTP 调用端点地址
     */
    private String endpoint;
    /**
     * HTTP 请求方法
     */
    private String httpMethod;
    /**
     * 输入参数的 JSON Schema
     */
    private String inputSchemaJson;
    /**
     * 调用超时时间（毫秒）
     */
    private int timeoutMs;
    /**
     * 创建时间
     */
    private Instant createdAt;
    private static final int DEFAULT_TIMEOUT_MS = 30_000;
    private static final String DEFAULT_HTTP_METHOD = "POST";


    /**
     * HTTP 方法枚举。
     */
    public enum HttpMethod {
        GET, POST, PUT, DELETE
    }
}
