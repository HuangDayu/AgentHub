package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.GuardrailStrategy;

import java.util.List;
import java.util.Optional;

public interface GuardrailStrategyRepository {
    GuardrailStrategy save(GuardrailStrategy strategy);
    Optional<GuardrailStrategy> findById(String id);
    List<GuardrailStrategy> findByWorkspace(String workspaceId);
    void deleteById(String id);
}
