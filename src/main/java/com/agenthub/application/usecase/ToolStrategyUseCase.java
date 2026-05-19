package com.agenthub.application.usecase;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.command.CreateToolStrategyCommand;
import com.agenthub.application.command.UpdateToolStrategyCommand;
import com.agenthub.application.port.out.repositories.ToolStrategyRepository;
import com.agenthub.domain.exception.NotFoundException;
import com.agenthub.domain.model.strategy.ToolStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author huangdayu
 */
@Component
@RequiredArgsConstructor
public class ToolStrategyUseCase {

    private final ToolStrategyRepository repository;


    public ToolStrategy update(String id, UpdateToolStrategyCommand command) {
        ToolStrategy strategy = BeanUtil.copyProperties(command, ToolStrategy.class);
        strategy.setId(id);
        return repository.saveOrUpdate(strategy);
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
        ToolStrategy strategy = BeanUtil.copyProperties(command, ToolStrategy.class);
        return repository.saveOrUpdate(strategy);
    }


}
