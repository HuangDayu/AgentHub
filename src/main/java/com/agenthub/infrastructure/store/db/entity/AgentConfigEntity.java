package com.agenthub.infrastructure.store.db.entity;
import lombok.Data;

import com.agenthub.common.annotations.ConfigChangeListenerEntity;
import com.baomidou.mybatisplus.annotation.*;

import java.time.Instant;

@Data
@TableName("agent_config")
public class AgentConfigEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String agentId;
    private String category;
    private String type;
    private String configId;
    private String name;
    private String description;
    private Integer priority;
    private boolean enabled;
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;
}
