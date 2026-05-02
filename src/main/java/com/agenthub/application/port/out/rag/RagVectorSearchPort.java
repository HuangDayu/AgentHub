package com.agenthub.application.port.out.rag;

import com.agenthub.domain.model.RetrievalResult;

import java.util.List;

/**
 * 向量搜索端口接口。
 * <p>
 * 定义基于向量相似度的检索操作契约。
 * </p>
 */
public interface RagVectorSearchPort {

    /**
     * 执行向量相似度搜索。
     *
     * @param kbId      知识库ID
     * @param queryText 查询文本
     * @param topK      返回结果数量
     * @return 检索结果列表
     */
    List<RetrievalResult> search(String kbId, String queryText, int topK);
}
