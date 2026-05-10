package com.agenthub.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author huangdayu
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeBaseCommand {

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

}
