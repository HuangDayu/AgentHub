package com.agenthub.infrastructure.persistence.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.agenthub.domain.model.Tool;
import com.agenthub.domain.model.ToolId;

import java.time.Instant;

/**
 * 工具注册表 MyBatis 实体。
 * <p>
 * 映射 tool_registry 表字段，包含 HTTP 调用所需的元数据。
 *
 * @since 1.0.0
 */
@TableName("app.tool_registry")
public class ToolEntity {
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

    public ToolEntity() {
    }

    /**
     * 将领域对象转换为 MyBatis 实体。
     *
     * @param tool 领域对象
     * @return MyBatis 实体
     */
    public static ToolEntity fromDomain(Tool tool) {
        ToolEntity entity = new ToolEntity();
        entity.setId(tool.id().value());
        entity.setName(tool.name());
        entity.setDescription(tool.description());
        entity.setEnabled(tool.enabled());
        entity.setEndpoint(tool.endpoint());
        entity.setInputSchema(tool.inputSchemaJson());
        entity.setTimeoutMs(tool.timeoutMs());
        entity.setCreatedAt(tool.createdAt());
        return entity;
    }

    /**
     * 将 MyBatis 实体转换为领域对象。
     *
     * @return 领域对象
     */
    public Tool toDomain() {
        return new Tool(
                ToolId.of(this.id),
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
