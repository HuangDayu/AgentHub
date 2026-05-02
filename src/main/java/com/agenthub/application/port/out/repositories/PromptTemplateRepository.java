package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.PromptTemplateInfo;

import java.util.List;
import java.util.Optional;

public interface PromptTemplateRepository {
    PromptTemplateInfo save(PromptTemplateInfo template);
    Optional<PromptTemplateInfo> findById(String id);
    List<PromptTemplateInfo> findByWorkspaceId(String workspaceId);
    List<PromptTemplateInfo> findByWorkspaceIdAndCategory(String workspaceId, String category);
    void deleteById(String id);
    PromptTemplateInfo update(PromptTemplateInfo template);
}
