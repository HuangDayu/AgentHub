package com.agenthub.application.usecase;

import com.agenthub.application.command.CreateToolStrategyCommand;
import com.agenthub.application.command.UpdateToolStrategyCommand;
import com.agenthub.application.port.out.repositories.ToolStrategyRepository;
import com.agenthub.common.exception.NotFoundException;
import com.agenthub.domain.model.ToolStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * @author huangdayu
 */
@Component
@RequiredArgsConstructor
public class ToolStrategyUseCase {

    private final ToolStrategyRepository repository;


    public ToolStrategy update(String id, UpdateToolStrategyCommand command) {
        ToolStrategy strategy = ToolStrategy.rebuild(id, command.workspaceId(), command.name(), command.description(),
                command.maxConcurrentCalls(), command.timeoutSeconds(), command.retryCount(), command.fallbackEnabled(), Instant.now(), Instant.now());
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
