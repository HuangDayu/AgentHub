package com.agenthub.domain.model;

import java.time.Instant;

/**
 * 工具领域模型。
 * <p>
 * 包含工具注册信息及 HTTP 调用所需的元数据。
 *
 * @since 1.0.0
 */
public record HttpTool(
        /** 工具唯一标识 */
        String id,
        /** 工具名称 */
        java.lang.String name,
        /** 工具描述 */
        java.lang.String description,
        /** 是否启用 */
        boolean enabled,
        /** HTTP 调用端点地址 */
        java.lang.String endpoint,
        /** HTTP 请求方法 */
        java.lang.String httpMethod,
        /** 输入参数的 JSON Schema */
        java.lang.String inputSchemaJson,
        /** 调用超时时间（毫秒） */
        int timeoutMs,
        /** 创建时间 */
        Instant createdAt) {
    private static final int DEFAULT_TIMEOUT_MS = 30_000;
    private static final java.lang.String DEFAULT_HTTP_METHOD = "POST";

    /**
     * 紧凑构造函数，执行验证和规范化。
     */
    public HttpTool {
        validateRequiredFields(id, name, createdAt);
        validateTimeout(timeoutMs);
        httpMethod = normalizeHttpMethod(httpMethod);
        timeoutMs = normalizeTimeout(timeoutMs);
    }

    /**
     * 验证必填字段。
     */
    private static void validateRequiredFields(String id, java.lang.String name, Instant createdAt) {
        if (id == null) throw new IllegalArgumentException("id must not be null");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name must not be blank");
        if (createdAt == null) throw new IllegalArgumentException("createdAt must not be null");
    }

    /**
     * 验证超时时间。
     */
    private static void validateTimeout(int timeoutMs) {
        if (timeoutMs < 0) throw new IllegalArgumentException("timeoutMs must not be negative");
    }

    /**
     * 规范化 HTTP 方法。
     */
    private static java.lang.String normalizeHttpMethod(java.lang.String httpMethod) {
        return httpMethod == null || httpMethod.isBlank() ? DEFAULT_HTTP_METHOD : httpMethod.toUpperCase();
    }

    /**
     * 规范化超时时间。
     */
    private static int normalizeTimeout(int timeoutMs) {
        return timeoutMs == 0 ? DEFAULT_TIMEOUT_MS : timeoutMs;
    }

    /**
     * 创建新工具（向后兼容重载）。
     */
    public static HttpTool create(String id, java.lang.String name, java.lang.String description, boolean enabled) {
        return create(id, name, description, enabled, null, null, null, 0);
    }

    /**
     * 创建新工具（含 HTTP 调用元数据）。
     */
    public static HttpTool create(String id, java.lang.String name, java.lang.String description, boolean enabled,
                                  java.lang.String endpoint, java.lang.String httpMethod, java.lang.String inputSchemaJson, int timeoutMs) {
        return new HttpTool(id, name.trim(), sanitizeDescription(description), enabled,
                endpoint, httpMethod, inputSchemaJson, timeoutMs, Instant.now());
    }

    /**
     * 清理描述文本。
     */
    private static java.lang.String sanitizeDescription(java.lang.String description) {
        return description == null ? "" : description.trim();
    }

    /**
     * 部分更新工具信息。
     */
    public HttpTool patch(java.lang.String name, java.lang.String description, Boolean enabled,
                          java.lang.String endpoint, java.lang.String httpMethod, java.lang.String inputSchemaJson, Integer timeoutMs) {
        return new HttpTool(this.id,
                resolveName(name),
                resolveDescription(description),
                resolveEnabled(enabled),
                resolveEndpoint(endpoint),
                resolveHttpMethod(httpMethod),
                resolveSchema(inputSchemaJson),
                resolveTimeout(timeoutMs),
                this.createdAt);
    }

    private java.lang.String resolveName(java.lang.String name) { return name == null ? this.name : name.trim(); }
    private java.lang.String resolveDescription(java.lang.String desc) { return desc == null ? this.description : sanitizeDescription(desc); }
    private boolean resolveEnabled(Boolean enabled) { return enabled == null ? this.enabled : enabled; }
    private java.lang.String resolveEndpoint(java.lang.String endpoint) { return endpoint == null ? this.endpoint : endpoint; }
    private java.lang.String resolveHttpMethod(java.lang.String method) { return method == null ? this.httpMethod : method.toUpperCase(); }
    private java.lang.String resolveSchema(java.lang.String schema) { return schema == null ? this.inputSchemaJson : schema; }
    private int resolveTimeout(Integer timeout) { return timeout == null ? this.timeoutMs : timeout; }

    /**
     * 向后兼容的 patch。
     */
    public HttpTool patch(java.lang.String name, java.lang.String description, Boolean enabled) {
        return patch(name, description, enabled, null, null, null, null);
    }

    /**
     * HTTP 方法枚举。
     */
    public enum HttpMethod {
        GET, POST, PUT, DELETE
    }
}
