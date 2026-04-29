package com.agenthub.domain.model;

import com.agenthub.common.exception.ValidationException;

import java.time.Instant;

/**
 * 知识库领域模型。
 * <p>
 * 表示一个知识库及其索引版本信息，支持版本管理和更新操作。
 * </p>
 */
public record KnowledgeBase(
        String id,
        /** 知识库唯一标识 */
        String kbCode,
        /** 知识库名称 */
        String name,
        /** 租户id */
        String tenantId,
        /** 知识库描述 */
        String description,
        /** 向量数据库配置 ID（可选，关联租户自建的向量库配置） */
        String vectorStoreConfigId,
        /** 嵌入模型配置 ID（可选，关联模型配置用于向量化） */
        String embeddingModelConfigId,
        /** 分词模型配置 ID（可选，关联模型配置用于分词） */
        String chatModelConfigId,
        /** 创建时间 */
        Instant createdAt,
        /** 最后更新时间 */
        Instant updatedAt
) {
    /**
     * 紧凑构造函数，验证参数。
     */
    public KnowledgeBase {
        validateKbCode(kbCode);
        validateName(name);
    }

    /**
     * 验证知识库代码。
     */
    private static void validateKbCode(String kbCode) {
        if (isBlank(kbCode)) {
            throw new ValidationException("kbCode must not be blank");
        }
    }

    /**
     * 验证名称。
     */
    private static void validateName(String name) {
        if (isBlank(name)) {
            throw new ValidationException("name must not be blank");
        }
    }

    /**
     * 创建知识库实例，自动处理索引版本和激活版本。
     *
     * @param kbCode                 知识库ID
     * @param name                   名称
     * @param description            描述
     * @param vectorStoreConfigId    向量数据库配置ID（可选）
     * @param embeddingModelConfigId 嵌入模型配置ID（可选）
     * @return 新的KnowledgeBase实例
     */
    public static KnowledgeBase create(
            String kbCode,
            String name,
            String description,
            String vectorStoreConfigId,
            String embeddingModelConfigId,
            String chatModelConfigId
    ) {
        Instant now = Instant.now();
        return new KnowledgeBase(null, kbCode, name, null, description , vectorStoreConfigId, embeddingModelConfigId, chatModelConfigId, now, now);
    }

    /**
     * 判断字符串是否为空。
     */
    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }





}
