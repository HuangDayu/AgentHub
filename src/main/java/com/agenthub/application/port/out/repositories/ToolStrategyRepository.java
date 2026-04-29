package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.ToolStrategy;

import java.util.List;
import java.util.Optional;

public interface ToolStrategyRepository {
    ToolStrategy save(ToolStrategy strategy);
    Optional<ToolStrategy> findById(String id);
    List<ToolStrategy> findByWorkspace(String workspaceId);
    void deleteById(String id);
}
