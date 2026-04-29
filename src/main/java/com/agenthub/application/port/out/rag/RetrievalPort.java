package com.agenthub.application.port.out.rag;

import com.agenthub.domain.model.RetrievalChunk;
import com.agenthub.domain.model.RetrievalQuery;

import java.util.List;

/**
 * 检索端口 - 执行知识检索
 */
public interface RetrievalPort {
    List<RetrievalChunk> retrieve(RetrievalQuery query);
}
