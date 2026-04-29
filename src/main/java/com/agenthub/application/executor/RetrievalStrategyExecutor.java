package com.agenthub.application.executor;

import com.agenthub.application.port.out.rag.RetrievalPort;
import com.agenthub.domain.model.RetrievalChunk;
import com.agenthub.domain.model.RetrievalQuery;
import com.agenthub.domain.model.RetrievalStrategy;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 检索策略执行器 - 执行知识检索
 */
@Component
public class RetrievalStrategyExecutor {

    private final RetrievalPort retrievalPort;

    public RetrievalStrategyExecutor(RetrievalPort retrievalPort) {
        this.retrievalPort = retrievalPort;
    }

    public List<RetrievalChunk> execute(RetrievalStrategy strategy, List<String> kbIds, String query) {
        if (kbIds.isEmpty()) return List.of();
        return retrieveFromKb(query, kbIds, strategy);
    }

    private List<RetrievalChunk> retrieveFromKb(String query, List<String> kbIds, RetrievalStrategy strategy) {
        return retrievalPort.retrieve(new RetrievalQuery(query, kbIds, strategy));
    }
}
