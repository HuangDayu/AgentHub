package com.agenthub.application.usecase;

import com.agenthub.common.exception.NotFoundException;
import com.agenthub.application.command.CreateToolStrategyCommand;
import com.agenthub.application.command.UpdateToolStrategyCommand;
import com.agenthub.application.port.out.repositories.ToolStrategyRepository;
import com.agenthub.domain.model.ToolStrategy;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author huangdayu
 */
@Component
public class ToolStrategyUseCase {

    private final ToolStrategyRepository repository;

    public ToolStrategyUseCase(ToolStrategyRepository repository) {
        this.repository = repository;
    }


    public ToolStrategy update(String id, UpdateToolStrategyCommand command) {
        ToolStrategy strategy = get(id);
        strategy.updateBasicInfo(command.name(), command.description());
        return repository.save(strategy);
    }


    public List<ToolStrategy> list(String workspaceId) {
        return repository.findByWorkspace(workspaceId);
    }


    public ToolStrategy get(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("ToolStrategy not found: " + id));
    }


    public void delete(String id) {
        get(id);
        repository.deleteById(id);
    }


    public ToolStrategy create(CreateToolStrategyCommand command) {
        ToolStrategy strategy = createStrategy(command);
        return repository.save(strategy);
    }

    private ToolStrategy createStrategy(CreateToolStrategyCommand cmd) {
        ToolStrategy strategy = ToolStrategy.create(cmd.workspaceId(), cmd.name());
        strategy.updateBasicInfo(cmd.name(), cmd.description());
        strategy.configureExecution(cmd.maxConcurrentCalls(), cmd.timeoutSeconds(), cmd.retryCount());
        return strategy;
    }

}
