package com.agenthub.application.usecase;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.command.KnowledgeBaseCommand;
import com.agenthub.application.port.out.repositories.KnowledgeBaseRepository;
import com.agenthub.common.exception.ConflictException;
import com.agenthub.common.exception.NotFoundException;
import com.agenthub.domain.model.KnowledgeBase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class KnowledgeBaseUseCase {
    private final KnowledgeBaseRepository repository;

    public KnowledgeBase create(KnowledgeBaseCommand knowledgeBaseCommand) {
        validateKbCode(knowledgeBaseCommand.getKbCode());
        KnowledgeBase kb = BeanUtil.copyProperties(knowledgeBaseCommand, KnowledgeBase.class);
        return repository.save(kb);
    }

    private void validateKbCode(String kbCode) {
        if (repository.existsByKbCode(kbCode)) {
            throw new ConflictException("knowledge base already exists: " + kbCode);
        }
    }


    public List<KnowledgeBase> list() {
        return repository.findAll();
    }

    public List<KnowledgeBase> list(String tenantId) {
        return repository.findByTenantId(tenantId);
    }

    public List<KnowledgeBase> listByWorkspace(String workspaceId) {
        return repository.findByWorkspace(workspaceId);
    }

    public KnowledgeBase getById(String kbId) {
        return repository.findById(kbId)
                .orElseThrow(() -> new NotFoundException("knowledge base not found: " + kbId));
    }

    public KnowledgeBase update(KnowledgeBaseCommand knowledgeBaseCommand) {
        KnowledgeBase knowledgeBase = BeanUtil.copyProperties(knowledgeBaseCommand, KnowledgeBase.class);
        return repository.save(knowledgeBase);
    }

    public void deleteById(String kbId) {
        getById(kbId);
        repository.deleteById(kbId);
    }


}
