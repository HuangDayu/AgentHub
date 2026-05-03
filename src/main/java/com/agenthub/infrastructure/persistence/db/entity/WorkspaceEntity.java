package com.agenthub.infrastructure.persistence.db.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.Instant;

/**
 * 工作区持久化实体。
 * 对应数据库表 app.workspace。
 */
@Data
@TableName("app.workspace")
public class WorkspaceEntity {
    @TableId(type = IdType.INPUT)
    private String id;
    private String tenantId;
    private String workspaceCode;
    private String name;
    private String region;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;


}
