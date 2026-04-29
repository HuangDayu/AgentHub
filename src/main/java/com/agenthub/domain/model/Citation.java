package com.agenthub.domain.model;

import com.agenthub.common.exception.ValidationException;

/**
 * 检索引用领域模型。
 * <p>
 * 表示检索结果中的文档引用，包含索引位置、文档ID和内容摘要。
 * </p>
 */
public record Citation(
        /** 引用在结果列表中的索引位置（从1开始） */
        int index,
        /** 所属文档ID */
        String documentId,
        /** 所属分块ID */
        String chunkId,
        /** 引用内容摘要 */
        String excerpt
) {
    public Citation {
        if (index <= 0) {
            throw new ValidationException("index must be positive");
        }
        if (isBlank(documentId)) {
            throw new ValidationException("documentId must not be blank");
        }
        if (isBlank(chunkId)) {
            throw new ValidationException("chunkId must not be blank");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
