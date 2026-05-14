package com.agenthub.application.usecase;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.command.CreateGuardrailStrategyCommand;
import com.agenthub.application.command.UpdateGuardrailStrategyCommand;
import com.agenthub.application.port.out.repositories.GuardrailStrategyRepository;
import com.agenthub.domain.exception.NotFoundException;
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
        GuardrailStrategy guardrailStrategy = BeanUtil.copyProperties(command, GuardrailStrategy.class);
        guardrailStrategy.setId(id);
        return repository.save(guardrailStrategy);
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
        GuardrailStrategy strategy = BeanUtil.copyProperties(command, GuardrailStrategy.class);
        return repository.save(strategy);
    }

}
