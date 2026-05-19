package com.agenthub.infrastructure.store.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

/**
 * 节点执行结果数据库实体.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("node_execution_result")
public class NodeExecutionResultEntity {

    /**
     * 主键ID.
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 执行ID.
     */
    @TableField(value = "execution_id")
    private String executionId;

    /**
     * 节点ID.
     */
    @TableField(value = "node_id")
    private String nodeId;

    /**
     * 节点名称.
     */
    @TableField(value = "node_name")
    private String nodeName;

    /**
     * 节点类型.
     */
    @TableField(value = "node_type")
    private String nodeType;

    /**
     * 执行状态.
     */
    private String status;

    /**
     * 输入参数.
     */
    private String input;

    /**
     * 输出结果.
     */
    private String output;

    /**
     * 错误信息.
     */
    @TableField(value = "error_info")
    private String errorInfo;

    /**
     * 开始时间.
     */
    @TableField(value = "start_time")
    private Instant startTime;

    /**
     * 结束时间.
     */
    @TableField(value = "end_time")
    private Instant endTime;

    /**
     * 执行时长（毫秒）.
     */
    private Long duration;

    /**
     * 创建时间.
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;
}
