package com.agenthub.api.controller;

import com.agenthub.api.dto.CreateSkillConfigRequest;
import com.agenthub.application.command.CreateSkillConfigCommand;
import com.agenthub.application.dto.SkillConfigOutput;
import com.agenthub.application.usecase.SkillConfigUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 技能配置控制器。
 */
@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/skill-configs")
@RequiredArgsConstructor
public class SkillConfigController {

    private final SkillConfigUseCase useCase;

    /**
     * 创建配置。
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SkillConfigOutput create(@PathVariable String workspaceId,
                                    @RequestBody CreateSkillConfigRequest request) {
        CreateSkillConfigCommand command = toCommand(request, workspaceId);
        return useCase.create(command);
    }

    /**
     * 列出配置。
     */
    @GetMapping
    public List<SkillConfigOutput> list(@PathVariable String workspaceId,
                                         @RequestParam String tenantId) {
        return useCase.list(tenantId, workspaceId);
    }

    /**
     * 获取配置。
     */
    @GetMapping("/{configId}")
    public SkillConfigOutput get(@PathVariable String workspaceId,
                                 @PathVariable String configId) {
        return useCase.get(configId);
    }

    /**
     * 更新配置。
     */
    @PutMapping("/{configId}")
    public SkillConfigOutput update(@PathVariable String workspaceId,
                                    @PathVariable String configId,
                                    @RequestBody CreateSkillConfigRequest request) {
        CreateSkillConfigCommand command = toCommand(request, workspaceId);
        return useCase.update(configId, command);
    }

    /**
     * 添加路径。
     */
    @PostMapping("/{configId}/paths")
    public SkillConfigOutput addPath(@PathVariable String workspaceId,
                                     @PathVariable String configId,
                                     @RequestParam String path) {
        return useCase.addSkillPath(configId, path);
    }

    /**
     * 移除路径。
     */
    @DeleteMapping("/{configId}/paths")
    public SkillConfigOutput removePath(@PathVariable String workspaceId,
                                        @PathVariable String configId,
                                        @RequestParam String path) {
        return useCase.removeSkillPath(configId, path);
    }

    /**
     * 删除配置。
     */
    @DeleteMapping("/{configId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String workspaceId,
                       @PathVariable String configId) {
        useCase.delete(configId);
    }

    /**
     * 将请求转换为命令。
     */
    private CreateSkillConfigCommand toCommand(CreateSkillConfigRequest request,
                                               String workspaceId) {
        CreateSkillConfigCommand command = new CreateSkillConfigCommand();
        command.setTenantId(request.getTenantId());
        command.setWorkspaceId(workspaceId);
        command.setName(request.getName());
        command.setDescription(request.getDescription());
        command.setSkillPaths(request.getSkillPaths());
        command.setSyncEnabled(request.isSyncEnabled());
        command.setSyncInterval(request.getSyncInterval());
        command.setAutoSync(request.isAutoSync());
        return command;
    }
}
