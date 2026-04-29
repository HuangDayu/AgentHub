package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.PromptTemplate;

import java.util.List;
import java.util.Optional;

public interface PromptTemplateRepository {
    PromptTemplate save(PromptTemplate template);
    Optional<PromptTemplate> findById(String id);
    List<PromptTemplate> findByWorkspaceId(String workspaceId);
    List<PromptTemplate> findByWorkspaceIdAndCategory(String workspaceId, String category);
    void deleteById(String id);
    PromptTemplate update(PromptTemplate template);
}
