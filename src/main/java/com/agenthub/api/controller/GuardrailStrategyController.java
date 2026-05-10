package com.agenthub.api.controller;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.api.dto.CreateGuardrailStrategyRequest;
import com.agenthub.api.dto.GuardrailStrategyResponse;
import com.agenthub.api.dto.UpdateGuardrailStrategyRequest;
import com.agenthub.api.mapper.GuardrailStrategyResponseMapper;
import com.agenthub.application.command.CreateGuardrailStrategyCommand;
import com.agenthub.application.command.UpdateGuardrailStrategyCommand;
import com.agenthub.application.usecase.GuardrailStrategyUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.agenthub.api.mapper.GuardrailStrategyResponseMapper.toResponse;

/**
 * 护栏策略API控制器。
 */
@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/guardrail-strategies")
public class GuardrailStrategyController {
    private final GuardrailStrategyUseCase guardrailStrategyUseCase;

    public GuardrailStrategyController(GuardrailStrategyUseCase guardrailStrategyUseCase) {
        this.guardrailStrategyUseCase = guardrailStrategyUseCase;
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GuardrailStrategyResponse create(
            @PathVariable String workspaceId,
            @RequestBody CreateGuardrailStrategyRequest request
    ) {
        CreateGuardrailStrategyCommand command = BeanUtil.copyProperties(request, CreateGuardrailStrategyCommand.class);
        return toResponse(guardrailStrategyUseCase.create(command));
    }


    @GetMapping
    public List<GuardrailStrategyResponse> list(@PathVariable String workspaceId) {
        return guardrailStrategyUseCase.list(workspaceId).stream()
                .map(GuardrailStrategyResponseMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public GuardrailStrategyResponse get(
            @PathVariable String workspaceId,
            @PathVariable String id
    ) {
        return toResponse(guardrailStrategyUseCase.get(id));
    }

    @PutMapping("/{id}")
    public GuardrailStrategyResponse update(
            @PathVariable String workspaceId,
            @PathVariable String id,
            @RequestBody UpdateGuardrailStrategyRequest request
    ) {
        UpdateGuardrailStrategyCommand command = BeanUtil.copyProperties(request, UpdateGuardrailStrategyCommand.class);
        return toResponse(guardrailStrategyUseCase.update(id, command));
    }


    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable String workspaceId,
            @PathVariable String id
    ) {
        guardrailStrategyUseCase.delete(id);
    }
}
