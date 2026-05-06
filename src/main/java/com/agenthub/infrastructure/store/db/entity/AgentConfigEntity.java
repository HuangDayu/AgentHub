package com.agenthub.infrastructure.store.db.entity;
import lombok.Data;

import com.baomidou.mybatisplus.annotation.*;

import java.time.Instant;

@Data
@TableName("app.agent_config")
public class AgentConfigEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String agentId;
    private String category;
    private String type;
    private String configId;
    private String description;
    private Integer priority;
    private boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;
}
