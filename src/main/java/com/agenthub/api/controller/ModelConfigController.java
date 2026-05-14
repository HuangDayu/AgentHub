package com.agenthub.api.controller;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.api.dto.ModelConfigRequest;
import com.agenthub.api.dto.ModelConfigResponse;
import com.agenthub.api.dto.ModelTestResponse;
import com.agenthub.api.mapper.ModelConfigResponseMapper;
import com.agenthub.application.command.CreateModelConfigCommand;
import com.agenthub.application.command.UpdateModelConfigCommand;
import com.agenthub.application.usecase.ModelConfigUseCase;
import com.agenthub.domain.exception.ModelNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 模型配置 API 控制器。
 */
@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/models")
public class ModelConfigController {

    private static final Logger log = LoggerFactory.getLogger(ModelConfigController.class);

    private final ModelConfigUseCase appService;
    private final ModelConfigResponseMapper responseMapper;

    public ModelConfigController(ModelConfigUseCase appService, ModelConfigResponseMapper responseMapper) {
        this.appService = appService;
        this.responseMapper = responseMapper;
    }

    /**
     * 创建模型配置。
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ModelConfigResponse create(
            @Validated @RequestBody ModelConfigRequest request) {
        CreateModelConfigCommand command = BeanUtil.copyProperties(request, CreateModelConfigCommand.class);
        var config = appService.create(command);
        return responseMapper.toResponse(config);
    }


    /**
     * 更新模型配置。
     */
    @PutMapping("/{id}")
    public ModelConfigResponse update(
            @PathVariable String id,
            @Validated @RequestBody ModelConfigRequest request) {
        UpdateModelConfigCommand command = BeanUtil.copyProperties(request, UpdateModelConfigCommand.class);
        command.setId(id);
        var config = appService.update(command);
        return responseMapper.toResponse(config);
    }

    /**
     * 获取模型配置详情。
     */
    @GetMapping("/{id}")
    public ModelConfigResponse getById(
            @PathVariable String id) {
        var config = appService.getById(id)
                .orElseThrow(() -> new ModelNotFoundException("Model config not found: id=" + id));
        return responseMapper.toResponse(config);
    }

    /**
     * 获取所有模型配置。
     */
    @GetMapping
    public List<ModelConfigResponse> listAll(
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "enabled", required = false) Boolean enabled) {
        if (type != null) {
            return responseMapper.toResponseList(appService.getListByType(type));
        }
        if (enabled != null && enabled) {
            return responseMapper.toResponseList(appService.getEnabledList());
        }
        return responseMapper.toResponseList(appService.getList());
    }

    /**
     * 删除模型配置。
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        boolean deleted = appService.deleteById(id);
        if (!deleted) {
            throw new ModelNotFoundException("Model config not found: id=" + id);
        }
    }

    /**
     * 测试模型配置。
     * <p>
     * 发送测试请求验证模型配置是否正确。
     * </p>
     */
    @PostMapping("/{id}/test")
    public ResponseEntity<ModelTestResponse> testModel(@PathVariable String id) {
        var result = appService.testModel(id);
        return ResponseEntity.ok(BeanUtil.copyProperties(result, ModelTestResponse.class));
    }
}
