package com.agenthub.domain.model;

import java.util.List;

/**
 * 检索查询。Agent 调用知识库检索时传入的参数。
 */
public record RetrievalQuery(
        String query, List<String> kbIds, RetrievalStrategy strategy
) {
}
