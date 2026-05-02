package com.agenthub.application.port.out.rag;

import com.agenthub.domain.model.RetrievalResult;

import java.util.List;

/**
 * 重排器端口接口。
 * <p>
 * 定义检索结果重排的业务操作契约。
 * </p>
 */
public interface RagRerankerPort {

    /**
     * 对检索结果进行重排。
     *
     * @param queryText 查询文本
     * @param results   原始检索结果
     * @return 重排后的检索结果
     */
    List<RetrievalResult> rerank(String queryText, List<RetrievalResult> results);
}
