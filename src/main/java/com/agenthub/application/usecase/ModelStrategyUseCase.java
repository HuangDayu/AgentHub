package com.agenthub.application.usecase;

import com.agenthub.common.exception.NotFoundException;
import com.agenthub.application.command.CreateModelStrategyCommand;
import com.agenthub.application.command.UpdateModelStrategyCommand;
import com.agenthub.application.port.out.repositories.ModelStrategyRepository;
import com.agenthub.domain.model.ModelStrategy;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author huangdayu
 */
@Component
public class ModelStrategyUseCase {

    private final ModelStrategyRepository repository;

    public ModelStrategyUseCase(ModelStrategyRepository repository) {
        this.repository = repository;
    }


    public ModelStrategy update(String id, UpdateModelStrategyCommand command) {
        ModelStrategy strategy = get(id);
        return repository.save(updateStrategy(strategy,command));
    }


    public List<ModelStrategy> list(String workspaceId) {
        return repository.findByWorkspace(workspaceId);
    }


    public ModelStrategy get(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("ModelStrategy not found: " + id));
    }


    public void delete(String id) {
        get(id);
        repository.deleteById(id);
    }

    public ModelStrategy create(CreateModelStrategyCommand command) {
        ModelStrategy strategy = createStrategy(command);
        return repository.save(strategy);
    }

    private ModelStrategy createStrategy(CreateModelStrategyCommand cmd) {
        ModelStrategy strategy = ModelStrategy.create(cmd.workspaceId(), cmd.name());
        strategy.updateBasicInfo(cmd.name(), cmd.description());
        strategy.configureParameters(cmd.temperature(), cmd.maxTokens(), cmd.topP());
        strategy.setPenalties(cmd.frequencyPenalty(), cmd.presencePenalty());
        return strategy;
    }

    private ModelStrategy updateStrategy(ModelStrategy strategy,UpdateModelStrategyCommand cmd) {
        strategy.updateBasicInfo(cmd.name(), cmd.description());
        strategy.configureParameters(cmd.temperature(), cmd.maxTokens(), cmd.topP());
        strategy.setPenalties(cmd.frequencyPenalty(), cmd.presencePenalty());
        return strategy;
    }

}
