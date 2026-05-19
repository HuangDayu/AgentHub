package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.strategy.ToolStrategy;

import java.util.List;
import java.util.Optional;

public interface ToolStrategyRepository {
    ToolStrategy saveOrUpdate(ToolStrategy strategy);
    Optional<ToolStrategy> findById(String id);
    List<ToolStrategy> findByWorkspace(String workspaceId);
    void deleteById(String id);
}
