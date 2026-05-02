package com.agenthub.application.usecase;

import com.agenthub.common.exception.NotFoundException;
import com.agenthub.application.command.CreateGuardrailStrategyCommand;
import com.agenthub.application.command.UpdateGuardrailStrategyCommand;
import com.agenthub.application.port.out.repositories.GuardrailStrategyRepository;
import com.agenthub.domain.model.GuardrailStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author huangdayu
 */
@Component
@RequiredArgsConstructor
public class GuardrailStrategyUseCase {

    private final GuardrailStrategyRepository repository;


    public List<GuardrailStrategy> list(String workspaceId) {
        return repository.findByWorkspace(workspaceId);
    }


    public GuardrailStrategy update(String id, UpdateGuardrailStrategyCommand command) {
        GuardrailStrategy strategy = get(id);
        strategy.updateBasicInfo(command.name(), command.description());
        return repository.save(strategy);
    }


    public GuardrailStrategy get(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("GuardrailStrategy not found: " + id));
    }


    public void delete(String id) {
        get(id);
        repository.deleteById(id);
    }


    public GuardrailStrategy create(CreateGuardrailStrategyCommand command) {
        GuardrailStrategy strategy = createStrategy(command);
        return repository.save(strategy);
    }

    private GuardrailStrategy createStrategy(CreateGuardrailStrategyCommand cmd) {
        GuardrailStrategy strategy = GuardrailStrategy.create(cmd.workspaceId(), cmd.name());
        strategy.updateBasicInfo(cmd.name(), cmd.description());
        configureValidation(strategy, cmd);
        configurePii(strategy, cmd);
        configureInjection(strategy, cmd);
        strategy.setLengthLimits(cmd.maxInputLength(), cmd.maxOutputLength());
        return strategy;
    }

    private void configureValidation(GuardrailStrategy s, CreateGuardrailStrategyCommand c) {
        if (c.inputValidationEnabled()) s.enableInputValidation();
        else s.disableInputValidation();
        if (c.outputValidationEnabled()) s.enableOutputValidation();
        else s.disableOutputValidation();
    }

    private void configurePii(GuardrailStrategy s, CreateGuardrailStrategyCommand c) {
        if (c.piiDetectionEnabled()) s.enablePiiDetection(c.piiMaskingEnabled());
        else s.disablePiiDetection();
    }

    private void configureInjection(GuardrailStrategy s, CreateGuardrailStrategyCommand c) {
        if (c.promptInjectionDetection()) s.enablePromptInjectionDetection();
        else s.disablePromptInjectionDetection();
    }

}
