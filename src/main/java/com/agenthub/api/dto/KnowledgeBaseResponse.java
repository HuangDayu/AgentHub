package com.agenthub.api.dto;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.domain.model.KnowledgeBase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 知识库响应DTO。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeBaseResponse {
    private String id;
    /**
     * 知识库唯一标识
     */
    private String kbCode;
    /**
     * 知识库名称
     */
    private String name;
    /**
     * 租户id *
     * private String  tenantId;
     * /** 知识库描述
     */
    private String description;
    /**
     * 向量数据库配置 ID（可选，关联租户自建的向量库配置）
     */
    private String vectorStoreConfigId;
    /**
     * 嵌入模型配置 ID（可选，关联模型配置用于向量化）
     */
    private String embeddingModelConfigId;
    /**
     * 分词模型配置 ID（可选，关联模型配置用于分词）
     */
    private String chatModelConfigId;
    /**
     * 创建时间
     */
    private Instant createdAt;
    /**
     * 最后更新时间
     */
    private Instant updatedAt;

    public static KnowledgeBaseResponse from(KnowledgeBase knowledgeBase) {
        return BeanUtil.copyProperties(knowledgeBase, KnowledgeBaseResponse.class);
    }
}
