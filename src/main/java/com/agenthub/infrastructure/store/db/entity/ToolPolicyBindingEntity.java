package com.agenthub.infrastructure.store.db.entity;
import lombok.Data;

import com.agenthub.infrastructure.store.db.mapper.ToolPolicyBindingMybatisMapper;
import com.baomidou.mybatisplus.annotation.*;

import java.time.Instant;

@Data
@TableName("tool_policy_binding")
public class ToolPolicyBindingEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String toolPolicyId;
    private String toolId;
    private Integer priority;
    private Boolean enabled;
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;

}
