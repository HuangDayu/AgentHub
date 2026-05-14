package com.agenthub.application.usecase;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.domain.exception.NotFoundException;
import com.agenthub.application.command.CreateModelStrategyCommand;
import com.agenthub.application.command.UpdateModelStrategyCommand;
import com.agenthub.application.port.out.repositories.ModelStrategyRepository;
import com.agenthub.domain.model.ModelStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author huangdayu
 */
@Component
@RequiredArgsConstructor
public class ModelStrategyUseCase {

    private final ModelStrategyRepository repository;


    public ModelStrategy update(String id, UpdateModelStrategyCommand command) {
        ModelStrategy strategy = BeanUtil.copyProperties(command, ModelStrategy.class);
        strategy.setId(id);
        return repository.save(strategy);
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
        ModelStrategy strategy = BeanUtil.copyProperties(command, ModelStrategy.class);
        return repository.save(strategy);
    }





}
