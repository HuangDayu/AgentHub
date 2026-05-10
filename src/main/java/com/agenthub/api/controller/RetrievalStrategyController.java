package com.agenthub.api.controller;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.api.dto.CreateRetrievalStrategyRequest;
import com.agenthub.api.dto.RetrievalStrategyResponse;
import com.agenthub.api.dto.UpdateRetrievalStrategyRequest;
import com.agenthub.api.mapper.RetrievalStrategyResponseMapper;
import com.agenthub.application.command.CreateRetrievalStrategyCommand;
import com.agenthub.application.command.UpdateRetrievalStrategyCommand;
import com.agenthub.application.usecase.RetrievalStrategyUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.agenthub.api.mapper.RetrievalStrategyResponseMapper.toResponse;

/**
 * 检索策略API控制器。
 */
@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/retrieval-strategies")
public class RetrievalStrategyController {
    private final RetrievalStrategyUseCase retrievalStrategyUseCase;

    public RetrievalStrategyController(RetrievalStrategyUseCase retrievalStrategyUseCase) {
        this.retrievalStrategyUseCase = retrievalStrategyUseCase;
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RetrievalStrategyResponse create(
            @PathVariable String workspaceId,
            @RequestBody CreateRetrievalStrategyRequest request
    ) {
        CreateRetrievalStrategyCommand command = BeanUtil.copyProperties(request, CreateRetrievalStrategyCommand.class);
        return toResponse(retrievalStrategyUseCase.create(command));
    }


    @GetMapping
    public List<RetrievalStrategyResponse> list(@PathVariable String workspaceId) {
        return retrievalStrategyUseCase.list(workspaceId).stream()
                .map(RetrievalStrategyResponseMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public RetrievalStrategyResponse get(
            @PathVariable String workspaceId,
            @PathVariable String id
    ) {
        return toResponse(retrievalStrategyUseCase.get(id));
    }

    @PutMapping("/{id}")
    public RetrievalStrategyResponse update(
            @PathVariable String workspaceId,
            @PathVariable String id,
            @RequestBody UpdateRetrievalStrategyRequest request
    ) {
        UpdateRetrievalStrategyCommand command = BeanUtil.copyProperties(request, UpdateRetrievalStrategyCommand.class);
        return toResponse(retrievalStrategyUseCase.update(id, command));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable String workspaceId,
            @PathVariable String id
    ) {
        retrievalStrategyUseCase.delete(id);
    }
}
