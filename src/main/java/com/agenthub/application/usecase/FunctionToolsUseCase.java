package com.agenthub.application.usecase;

import com.agenthub.application.dto.FunctionToolOutput;
import com.agenthub.application.port.out.repositories.FunctionToolsRepository;
import com.agenthub.application.port.out.tools.FunctionToolScannerPort;
import com.agenthub.domain.model.FunctionTool;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FunctionToolsUseCase {

    private final FunctionToolsRepository repository;
    private final FunctionToolScannerPort scanner;

    public FunctionToolsUseCase(FunctionToolsRepository repository, FunctionToolScannerPort scanner) {
        this.repository = repository;
        this.scanner = scanner;
    }

    @Transactional
    public void syncTools() {
        List<FunctionTool> tools = scanner.scanFunctionTools();
        for (FunctionTool tool : tools) { syncTool(tool); }
    }

    private void syncTool(FunctionTool tool) {
        repository.findByToolClassName(tool.getToolClassName())
            .ifPresentOrElse(e -> updateTool(e, tool), () -> repository.save(tool));
    }

    private void updateTool(FunctionTool existing, FunctionTool newTool) {
        existing.update(newTool.getToolName(), newTool.getDescription(), 
                       newTool.getCategory(), newTool.getMethodCount());
        repository.save(existing);
    }

    public List<FunctionToolOutput> listAll() {
        return repository.findAll().stream().map(this::toOutput).toList();
    }

    public List<FunctionToolOutput> listEnabled() {
        return repository.findByEnabled(true).stream().map(this::toOutput).toList();
    }

    public FunctionToolOutput getById(String id) {
        return toOutput(repository.findById(id).orElseThrow());
    }

    public FunctionToolOutput enable(String id) {
        repository.updateEnabled(id, true);
        return getById(id);
    }

    public FunctionToolOutput disable(String id) {
        repository.updateEnabled(id, false);
        return getById(id);
    }

    public void delete(String id) { repository.deleteById(id); }

    private FunctionToolOutput toOutput(FunctionTool t) {
        return new FunctionToolOutput(t.getId(), t.getTenantId(), t.getToolClassName(),
            t.getToolName(), t.getDescription(), t.getCategory(), t.getMethodCount(),
            t.isEnabled(), t.isSystemTool(), t.getCreatedAt(), t.getUpdatedAt());
    }
}
