package com.agenthub.application.usecase;

import com.agenthub.application.dto.SystemToolOutput;
import com.agenthub.application.port.out.repositories.SystemToolsRepository;
import com.agenthub.application.port.out.tools.SystemToolScannerPort;
import com.agenthub.domain.model.SystemTool;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SystemToolsUseCase {

    private final SystemToolsRepository repository;
    private final SystemToolScannerPort scanner;

    public SystemToolsUseCase(SystemToolsRepository repository, SystemToolScannerPort scanner) {
        this.repository = repository;
        this.scanner = scanner;
    }

    @Transactional
    public void syncTools() {
        List<SystemTool> tools = scanner.scanSystemTools();
        repository.syncTools(tools);
    }

    public List<SystemToolOutput> listAll() {
        return repository.findAll().stream().map(this::toOutput).toList();
    }

    public List<SystemToolOutput> listEnabled() {
        return repository.findByEnabled(true).stream().map(this::toOutput).toList();
    }

    public SystemToolOutput getById(String id) {
        return toOutput(repository.findById(id).orElseThrow());
    }

    public SystemToolOutput enable(String id) {
        repository.updateEnabled(id, true);
        return getById(id);
    }

    public SystemToolOutput disable(String id) {
        repository.updateEnabled(id, false);
        return getById(id);
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    private SystemToolOutput toOutput(SystemTool t) {
        return new SystemToolOutput(t.getId(), t.getTenantId(), t.getToolClassName(),
                t.getToolName(), t.getDescription(), t.getCategory(), t.getMethodCount(),
                t.isEnabled(), t.isSystemTool(), t.getCreatedAt(), t.getUpdatedAt());
    }
}
