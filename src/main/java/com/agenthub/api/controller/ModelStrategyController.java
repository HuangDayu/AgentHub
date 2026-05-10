package com.agenthub.api.controller;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.api.dto.CreateModelStrategyRequest;
import com.agenthub.api.dto.ModelStrategyResponse;
import com.agenthub.api.dto.UpdateModelStrategyRequest;
import com.agenthub.api.mapper.ModelStrategyResponseMapper;
import com.agenthub.application.command.CreateModelStrategyCommand;
import com.agenthub.application.command.UpdateModelStrategyCommand;
import com.agenthub.application.usecase.ModelStrategyUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.agenthub.api.mapper.ModelStrategyResponseMapper.toResponse;

/**
 * 模型策略API控制器。
 */
@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/model-strategies")
public class ModelStrategyController {
    private final ModelStrategyUseCase modelStrategyUseCase;

    public ModelStrategyController(ModelStrategyUseCase modelStrategyUseCase) {
        this.modelStrategyUseCase = modelStrategyUseCase;
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ModelStrategyResponse create(
            @PathVariable String workspaceId,
            @RequestBody CreateModelStrategyRequest request
    ) {
        CreateModelStrategyCommand command = BeanUtil.copyProperties(request, CreateModelStrategyCommand.class);
        return toResponse(modelStrategyUseCase.create(command));
    }



    @GetMapping
    public List<ModelStrategyResponse> list(@PathVariable String workspaceId) {
        return modelStrategyUseCase.list(workspaceId).stream()
                .map(ModelStrategyResponseMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public ModelStrategyResponse get(
            @PathVariable String workspaceId,
            @PathVariable String id
    ) {
        return toResponse(modelStrategyUseCase.get(id));
    }

    @PutMapping("/{id}")
    public ModelStrategyResponse update(
            @PathVariable String workspaceId,
            @PathVariable String id,
            @RequestBody UpdateModelStrategyRequest request
    ) {
        return toResponse(modelStrategyUseCase.update(id, BeanUtil.copyProperties(request, UpdateModelStrategyCommand.class)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable String workspaceId,
            @PathVariable String id
    ) {
        modelStrategyUseCase.delete(id);
    }
}
