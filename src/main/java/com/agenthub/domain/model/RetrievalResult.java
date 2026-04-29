package com.agenthub.domain.model;

import com.agenthub.common.exception.ValidationException;

/**
 * 检索结果领域模型。
 * <p>
 * 表示单个检索结果项，包含文档ID、分块ID、内容和相关性分数。
 * </p>
 */
public record RetrievalResult(
        /** 所属文档ID */
        String documentId,
        /** 文档标题 */
        String documentTitle,
        /** 所属分块ID */
        String chunkId,
        /** 分块内容 */
        String content,
        /** 相关性分数（0-1之间） */
        double score
) {
    public RetrievalResult {
        if (isBlank(documentId)) {
            throw new ValidationException("documentId must not be blank");
        }
        if (isBlank(chunkId)) {
            throw new ValidationException("chunkId must not be blank");
        }
        if (isBlank(content)) {
            throw new ValidationException("content must not be blank");
        }
        if (score < 0 || score > 1) {
            throw new ValidationException("score must be between 0 and 1");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
