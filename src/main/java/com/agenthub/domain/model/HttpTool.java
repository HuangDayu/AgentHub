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
        HttpToolId id,
        /** 工具名称 */
        String name,
        /** 工具描述 */
        String description,
        /** 是否启用 */
        boolean enabled,
        /** HTTP 调用端点地址 */
        String endpoint,
        /** HTTP 请求方法 */
        String httpMethod,
        /** 输入参数的 JSON Schema */
        String inputSchemaJson,
        /** 调用超时时间（毫秒） */
        int timeoutMs,
        /** 创建时间 */
        Instant createdAt) {
    private static final int DEFAULT_TIMEOUT_MS = 30_000;
    private static final String DEFAULT_HTTP_METHOD = "POST";

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
    private static void validateRequiredFields(HttpToolId id, String name, Instant createdAt) {
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
    private static String normalizeHttpMethod(String httpMethod) {
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
    public static HttpTool create(HttpToolId id, String name, String description, boolean enabled) {
        return create(id, name, description, enabled, null, null, null, 0);
    }

    /**
     * 创建新工具（含 HTTP 调用元数据）。
     */
    public static HttpTool create(HttpToolId id, String name, String description, boolean enabled,
                                  String endpoint, String httpMethod, String inputSchemaJson, int timeoutMs) {
        return new HttpTool(id, name.trim(), sanitizeDescription(description), enabled,
                endpoint, httpMethod, inputSchemaJson, timeoutMs, Instant.now());
    }

    /**
     * 清理描述文本。
     */
    private static String sanitizeDescription(String description) {
        return description == null ? "" : description.trim();
    }

    /**
     * 部分更新工具信息。
     */
    public HttpTool patch(String name, String description, Boolean enabled,
                          String endpoint, String httpMethod, String inputSchemaJson, Integer timeoutMs) {
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

    private String resolveName(String name) { return name == null ? this.name : name.trim(); }
    private String resolveDescription(String desc) { return desc == null ? this.description : sanitizeDescription(desc); }
    private boolean resolveEnabled(Boolean enabled) { return enabled == null ? this.enabled : enabled; }
    private String resolveEndpoint(String endpoint) { return endpoint == null ? this.endpoint : endpoint; }
    private String resolveHttpMethod(String method) { return method == null ? this.httpMethod : method.toUpperCase(); }
    private String resolveSchema(String schema) { return schema == null ? this.inputSchemaJson : schema; }
    private int resolveTimeout(Integer timeout) { return timeout == null ? this.timeoutMs : timeout; }

    /**
     * 向后兼容的 patch。
     */
    public HttpTool patch(String name, String description, Boolean enabled) {
        return patch(name, description, enabled, null, null, null, null);
    }

    /**
     * HTTP 方法枚举。
     */
    public enum HttpMethod {
        GET, POST, PUT, DELETE
    }
}
