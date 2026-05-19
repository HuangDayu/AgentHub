package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.strategy.RetrievalStrategy;

import java.util.List;
import java.util.Optional;

public interface RetrievalStrategyRepository {
    RetrievalStrategy save(RetrievalStrategy strategy);
    Optional<RetrievalStrategy> findById(String id);
    List<RetrievalStrategy> findByWorkspace(String workspaceId);
    void deleteById(String id);
}
