package com.agenthub.application.usecase;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.command.MemoryCommand;
import com.agenthub.application.dto.MemoryOutput;
import com.agenthub.application.port.out.repositories.MemoryRepository;
import com.agenthub.domain.exception.NotFoundException;
import com.agenthub.domain.model.Memory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MemoryUseCase {
    private final MemoryRepository repository;

    public MemoryOutput create(MemoryCommand command) {
        Memory memory = BeanUtil.copyProperties(command, Memory.class);
        return toOutput(repository.save(memory));
    }

    public MemoryOutput get(String memoryId) {
        return toOutput(findById(memoryId));
    }

    public List<MemoryOutput> listByAgent(String agentId) {
        return repository.findByAgentId(agentId).stream().map(this::toOutput).toList();
    }

    public MemoryOutput update(MemoryCommand command) {
        Memory memory = BeanUtil.copyProperties(command, Memory.class);
        return toOutput(repository.save(memory));
    }

    public void delete(String memoryId) {
        findById(memoryId);
        repository.deleteById(memoryId);
    }

    public void deleteByAgent(String agentId) {
        repository.deleteByAgentId(agentId);
    }

    private Memory findById(String memoryId) {
        return repository.findById(memoryId)
                .orElseThrow(() -> new NotFoundException("Memory not found: " + memoryId));
    }

    private MemoryOutput toOutput(Memory memory) {
        return new MemoryOutput(memory.getId(), memory.getTenantId(), memory.getWorkspaceId(),
                memory.getAgentId(), memory.getName(), memory.getMemoryType().name(), memory.getContent(),
                memory.getMetadata(), memory.getImportance(), memory.getExpiresAt(),
                memory.getCreatedAt(), memory.getUpdatedAt());
    }
}
