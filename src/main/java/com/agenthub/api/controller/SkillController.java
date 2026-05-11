package com.agenthub.api.controller;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.api.dto.CreateSkillRequest;
import com.agenthub.api.dto.SkillResponse;
import com.agenthub.application.command.SkillRequestCommand;
import com.agenthub.application.dto.SkillOutput;
import com.agenthub.application.usecase.SkillUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/skills")
public class SkillController {
    private final SkillUseCase useCase;

    public SkillController(SkillUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SkillResponse create(@RequestBody CreateSkillRequest request) {
        SkillOutput result = useCase.create(BeanUtil.copyProperties(request, SkillRequestCommand.class));
        return toResponse(result);
    }

    @PostMapping("/sync")
    public void sync() {
        useCase.sync();
    }

    @GetMapping
    public List<SkillResponse> list() {
        return useCase.list().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{skillId}")
    public SkillResponse get(@PathVariable String skillId) {
        return toResponse(useCase.get(skillId));
    }

    @PutMapping("/{skillId}")
    public SkillResponse update(@PathVariable String skillId,
                                @RequestBody CreateSkillRequest request) {
        SkillOutput result = useCase.update(skillId, BeanUtil.copyProperties(request, SkillRequestCommand.class));
        return toResponse(result);
    }

    @PostMapping("/{skillId}/enable")
    public SkillResponse enable(@PathVariable String skillId) {
        return toResponse(useCase.enable(skillId));
    }

    @PostMapping("/{skillId}/disable")
    public SkillResponse disable(@PathVariable String skillId) {
        return toResponse(useCase.disable(skillId));
    }

    @DeleteMapping("/{skillId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String skillId) {
        useCase.delete(skillId);
    }

    private SkillResponse toResponse(SkillOutput result) {
        return BeanUtil.copyProperties(result, SkillResponse.class);
    }
}
