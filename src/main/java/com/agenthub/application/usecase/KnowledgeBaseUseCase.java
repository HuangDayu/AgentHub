package com.agenthub.application.usecase;

import com.agenthub.common.exception.ConflictException;
import com.agenthub.common.exception.NotFoundException;
import com.agenthub.application.port.out.repositories.KnowledgeBaseRepository;
import com.agenthub.domain.model.KnowledgeBase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class KnowledgeBaseUseCase {
    private final KnowledgeBaseRepository repository;

    public KnowledgeBase create(Command command) {
        validateKbCode(command.kbCode());
        KnowledgeBase kb = buildKnowledgeBase(command);
        return repository.save(kb);
    }

    private void validateKbCode(String kbCode) {
        if (repository.existsByKbCode(kbCode)) {
            throw new ConflictException("knowledge base already exists: " + kbCode);
        }
    }

    private KnowledgeBase buildKnowledgeBase(Command command) {
        return KnowledgeBase.create(command.kbCode(), command.name(), command.description(),
                command.vectorStoreConfigId(), command.embeddingModelConfigId(), command.chatModelConfigId());
    }

    public List<KnowledgeBase> list() { return repository.findAll(); }

    public List<KnowledgeBase> list(String tenantId) { return repository.findByTenantId(tenantId); }

    public List<KnowledgeBase> listByWorkspace(String workspaceId) {
        return repository.findByWorkspace(workspaceId);
    }

    public KnowledgeBase getById(String kbId) {
        return repository.findById(kbId)
                .orElseThrow(() -> new NotFoundException("knowledge base not found: " + kbId));
    }

    public KnowledgeBase update(Command command) {
        KnowledgeBase existing = getById(command.kbId());
        return repository.save(buidldPatchedKnowledgeBase(existing,command));
    }

    public void deleteById(String kbId) {
        getById(kbId);
        repository.deleteById(kbId);
    }

    public record Command(
            String kbId, String tenantId, String workspaceId, String name, String kbCode,
            String description, String vectorStoreConfigId, String embeddingModelConfigId,
            String chatModelConfigId, String retrievalPolicy
    ) {}

    /**
     * 部分更新知识库的名称和描述。
     *
     * @return 更新后的KnowledgeBase实例
     */
    public KnowledgeBase buidldPatchedKnowledgeBase(KnowledgeBase existing,KnowledgeBaseUseCase.Command command) {
        String patchedName = command.name() == null ? existing.name() : command.name();
        String patchedDescription = command.description() == null ? existing.description() : command.description();
        return new KnowledgeBase(
                existing.id(),
                existing.kbCode(),
                patchedName,
                existing.tenantId(),
                patchedDescription,
                command.vectorStoreConfigId(),
                command.embeddingModelConfigId(),
                command.chatModelConfigId(),
                existing.createdAt(),
                Instant.now()
        );
    }
}
