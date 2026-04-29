package com.agenthub.api.controller;

import com.agenthub.api.dto.VectorStoreConfigRequest;
import com.agenthub.api.dto.VectorStoreConfigResponse;
import com.agenthub.api.dto.VectorStoreTestResponse;
import com.agenthub.application.command.CreateVectorStoreConfigCommand;
import com.agenthub.application.command.UpdateVectorStoreConfigCommand;
import com.agenthub.application.usecase.VectorStoreConfigUseCase;
import com.agenthub.application.usecase.ManageVectorStoreUseCase;
import com.agenthub.domain.model.VectorStoreConfig;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 向量库配置 REST API 控制器。
 * <p>
 * 提供向量库配置的 CRUD 操作接口。
 * </p>
 */
@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/vector-stores")
public class VectorStoreConfigController {
    
    private final VectorStoreConfigUseCase vectorStoreConfigUseCase;
    private final ManageVectorStoreUseCase manageVectorStoreUseCase;


    public VectorStoreConfigController(
            VectorStoreConfigUseCase vectorStoreConfigUseCase,
            ManageVectorStoreUseCase manageVectorStoreUseCase) {
        this.vectorStoreConfigUseCase = vectorStoreConfigUseCase;
        this.manageVectorStoreUseCase = manageVectorStoreUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VectorStoreConfigResponse create(@RequestBody VectorStoreConfigRequest request) {
        CreateVectorStoreConfigCommand command = buildCreateCommand(request);
        VectorStoreConfig config = vectorStoreConfigUseCase.create(command);
        return VectorStoreConfigResponse.from(config);
    }

    private CreateVectorStoreConfigCommand buildCreateCommand(VectorStoreConfigRequest request) {
        return new CreateVectorStoreConfigCommand(
                request.name(), request.type(), request.host(), request.port(),
                request.apiKey(), request.collectionName(), request.extraParams());
    }

    @GetMapping
    public List<VectorStoreConfigResponse> listAll() {
        List<VectorStoreConfig> configs = vectorStoreConfigUseCase.findAll().stream().toList();
        return configs.stream().map(VectorStoreConfigResponse::from).toList();
    }

    @GetMapping("/{configId}")
    public VectorStoreConfigResponse getById(@PathVariable String configId) {
        VectorStoreConfig config = vectorStoreConfigUseCase.getById(configId);
        return VectorStoreConfigResponse.from(config);
    }

    @PutMapping("/{configId}")
    public VectorStoreConfigResponse update(
            @PathVariable String configId, @RequestBody VectorStoreConfigRequest request) {
        UpdateVectorStoreConfigCommand command = buildUpdateCommand(configId, request);
        VectorStoreConfig config = vectorStoreConfigUseCase.update(command);
        return VectorStoreConfigResponse.from(config);
    }

    private UpdateVectorStoreConfigCommand buildUpdateCommand(String configId, VectorStoreConfigRequest request) {
        return new UpdateVectorStoreConfigCommand(
                configId, request.name(), request.host(), request.port(),
                request.apiKey(), request.collectionName(), request.extraParams(), request.enabled());
    }

    @DeleteMapping("/{configId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String configId) {
        manageVectorStoreUseCase.deleteConfigAndClearCache(configId);
    }

    @PostMapping("/{configId}/refresh")
    public ResponseEntity<Void> refresh(@PathVariable String configId) {
        manageVectorStoreUseCase.refreshVectorStore(configId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{configId}/instance")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void destroyInstance(@PathVariable String configId) {
        manageVectorStoreUseCase.destroyInstance(configId);
    }

    @PostMapping("/{configId}/test")
    public ResponseEntity<VectorStoreTestResponse> testConnection(@PathVariable String configId) {
        var result = vectorStoreConfigUseCase.testConnection(configId);
        return ResponseEntity.ok(new VectorStoreTestResponse(
                result.success(), result.message(), result.details()));
    }

}
