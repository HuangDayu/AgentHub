package com.agenthub.infrastructure.store.db.entity;
import com.agenthub.common.annotations.ConfigChangeListenerEntity;
import com.agenthub.domain.annotation.AgentDataField;
import com.agenthub.domain.annotation.AgentDataModel;
import com.agenthub.domain.enums.RetrievalType;
import com.agenthub.infrastructure.store.db.mapper.RetrievalStrategyMybatisMapper;
import lombok.Data;

import com.baomidou.mybatisplus.annotation.*;

import java.time.Instant;

import static com.agenthub.domain.enums.AgentConfigCategory.STRATEGY;
import static com.agenthub.domain.enums.AgentConfigType.RETRIEVAL_STRATEGY;

@Data
@TableName("retrieval_policy")
@ConfigChangeListenerEntity(category = STRATEGY, type = RETRIEVAL_STRATEGY)
@AgentDataModel(
    name = "检索策略",
    description = "检索策略配置，管理检索类型与参数",
    domain = "策略管理",
    mapper = RetrievalStrategyMybatisMapper.class
)
public class RetrievalStrategyEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    @AgentDataField(hidden = true)
    @TableField(value = "tenant_id", fill = FieldFill.INSERT)
    private String tenantId;

    @AgentDataField(hidden = true)
    @TableField(value = "workspace_id", fill = FieldFill.INSERT)
    private String workspaceId;

    @AgentDataField(description = "策略名称", filterable = true)
    private String name;

    @AgentDataField(description = "策略描述")
    private String description;

    @AgentDataField(description = "检索类型", filterable = true, enumType = RetrievalType.class)
    private String retrievalType;

    @AgentDataField(description = "返回结果数")
    private Integer topK;

    @AgentDataField(description = "分数阈值")
    private Double scoreThreshold;

    @AgentDataField(description = "是否启用重排序")
    private Boolean enableRerank;

    @AgentDataField(description = "是否启用查询改写")
    private Boolean enableQueryRewrite;

    @AgentDataField(description = "是否启用文本搜索")
    private Boolean enableTextSearch;

    @AgentDataField(description = "是否启用向量搜索")
    private Boolean enableVectorSearch;

    @AgentDataField(description = "重排序模型")
    private String rerankModel;

    @AgentDataField(description = "向量权重")
    private Double vectorWeight;

    @AgentDataField(description = "关键词权重")
    private Double keywordWeight;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;
}
