package com.agenthub.infrastructure.store.db.entity;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.common.annotations.ConfigChangeListenerEntity;
import com.agenthub.domain.annotation.AgentDataField;
import com.agenthub.domain.annotation.AgentDataModel;
import com.agenthub.domain.enums.HttpAuthType;
import com.agenthub.domain.model.tools.HttpTool;
import com.agenthub.infrastructure.store.db.mapper.HttpToolMybatisMapper;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.Instant;

import static com.agenthub.domain.enums.AgentConfigCategory.TOOL;
import static com.agenthub.domain.enums.AgentConfigType.HTTP_TOOL;

/**
 * 工具注册表 MyBatis 实体。
 * <p>
 * 映射 tool_registry 表字段，包含 HTTP 调用所需的元数据。
 *
 * @since 1.0.0
 */
@Data
@TableName("http_tools")
@ConfigChangeListenerEntity(category = TOOL, type = HTTP_TOOL)
@AgentDataModel(
    name = "HTTP工具",
    description = "HTTP接口工具，通过REST API调用外部服务",
    domain = "工具管理",
    mapper = HttpToolMybatisMapper.class
)
public class HttpToolsEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    @AgentDataField(hidden = true)
    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private String tenantId;
    @AgentDataField(hidden = true)
    @TableField(value = "workspace_id", fill = FieldFill.INSERT)
    private String workspaceId;
    @AgentDataField(description = "工具名称", required = true, filterable = true)
    private String name;
    @AgentDataField(description = "工具描述")
    private String description;
    @AgentDataField(description = "是否启用")
    private boolean enabled;
    @AgentDataField(description = "接口地址")
    private String endpoint;
    @AgentDataField(description = "认证方式", enumType = HttpAuthType.class)
    private String authType;
    @AgentDataField(description = "输入参数Schema")
    private String inputSchema;
    @AgentDataField(description = "超时时间(ms)")
    private int timeoutMs;
    @TableField(value = "created_at", fill = FieldFill.INSERT)
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
        return BeanUtil.copyProperties(httpTool, HttpToolsEntity.class);
    }

    /**
     * 将 MyBatis 实体转换为领域对象。
     *
     * @return 领域对象
     */
    public HttpTool toDomain() {
        return new HttpTool(this.id, this.name, this.description, this.enabled,
                this.endpoint, this.authType, this.inputSchema,
                this.timeoutMs, this.createdAt);
    }

}
