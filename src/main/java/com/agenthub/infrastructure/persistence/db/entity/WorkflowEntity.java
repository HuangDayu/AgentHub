package com.agenthub.infrastructure.persistence.db.entity;
import lombok.Data;

import com.baomidou.mybatisplus.annotation.*;

import java.time.Instant;

@Data
@TableName("app.workflow")
public class WorkflowEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private String tenantId;
    @TableField(value = "workspace_id", fill = FieldFill.INSERT)
    private String workspaceId;
    @TableField(value = "workflow_code")
    private String workflowCode;
    private String name;
    private String description;
    @TableField(value = "graph_definition")
    private String graphDefinition;
    private String status;
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;
    @TableField(value = "updated_at", fill = FieldFill.INSERT)
    private Instant updatedAt;

}
