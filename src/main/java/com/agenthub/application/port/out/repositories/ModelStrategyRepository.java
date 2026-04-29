package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.ModelStrategy;

import java.util.List;
import java.util.Optional;

public interface ModelStrategyRepository {
    ModelStrategy save(ModelStrategy strategy);
    Optional<ModelStrategy> findById(String id);
    List<ModelStrategy> findByWorkspace(String workspaceId);
    void deleteById(String id);
}
