package com.agenthub.infrastructure.persistence.db.entity;

import com.agenthub.domain.model.HttpTool;
import com.baomidou.mybatisplus.annotation.*;
import com.agenthub.domain.model.HttpToolId;

import java.time.Instant;

/**
 * 工具注册表 MyBatis 实体。
 * <p>
 * 映射 tool_registry 表字段，包含 HTTP 调用所需的元数据。
 *
 * @since 1.0.0
 */
@TableName("app.http_tools")
public class HttpToolsEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String name;
    private String description;
    private boolean enabled;
    private String endpoint;
    private String authType;
    private String inputSchema;
    private int timeoutMs;
    @TableField(value = "created_at",fill = FieldFill.INSERT)
    private Instant createdAt;

    public HttpToolsEntity() {
    }

    /**
     * 将领域对象转换为 MyBatis 实体。
     *
     * @param httpTool 领域对象
     * @return MyBatis 实体
     */
    public static HttpToolsEntity fromDomain(HttpTool httpTool) {
        HttpToolsEntity entity = new HttpToolsEntity();
        entity.setId(httpTool.id().value());
        entity.setName(httpTool.name());
        entity.setDescription(httpTool.description());
        entity.setEnabled(httpTool.enabled());
        entity.setEndpoint(httpTool.endpoint());
        entity.setInputSchema(httpTool.inputSchemaJson());
        entity.setTimeoutMs(httpTool.timeoutMs());
        entity.setCreatedAt(httpTool.createdAt());
        return entity;
    }

    /**
     * 将 MyBatis 实体转换为领域对象。
     *
     * @return 领域对象
     */
    public HttpTool toDomain() {
        return new HttpTool(
                HttpToolId.of(this.id),
                this.name,
                this.description,
                this.enabled,
                this.endpoint,
                this.authType,
                this.inputSchema,
                this.timeoutMs,
                this.createdAt
        );
    }

    // ==================== Getter / Setter ====================

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public String getInputSchema() {
        return inputSchema;
    }

    public void setInputSchema(String inputSchema) {
        this.inputSchema = inputSchema;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
