package com.agenthub.api.controller;

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
        CreateModelStrategyCommand command = buildCreateCommand(workspaceId, request);
        return toResponse(modelStrategyUseCase.create(command));
    }

    private CreateModelStrategyCommand buildCreateCommand(String workspaceId, CreateModelStrategyRequest req) {
        return new CreateModelStrategyCommand(
                workspaceId, req.name(), req.description(),
                req.temperature() != null ? req.temperature() : 0.7,
                req.maxTokens() != null ? req.maxTokens() : 2048,
                req.topP() != null ? req.topP() : 1.0,
                req.frequencyPenalty() != null ? req.frequencyPenalty() : 0.0,
                req.presencePenalty() != null ? req.presencePenalty() : 0.0
        );
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
        UpdateModelStrategyCommand command = new UpdateModelStrategyCommand(
                id, request.name(), request.description()
                , request.temperature() != null ? request.temperature() : 0.7
                , request.maxTokens() != null ? request.maxTokens() : 2048
                , request.topP() != null ? request.topP() : 1.0
                , request.frequencyPenalty() != null ? request.frequencyPenalty() : 0.0
                , request.presencePenalty() != null ? request.presencePenalty() : 0.0
        );
        return toResponse(modelStrategyUseCase.update(id, command));
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
