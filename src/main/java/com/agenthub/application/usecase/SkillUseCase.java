package com.agenthub.application.usecase;

import com.agenthub.common.exception.NotFoundException;
import com.agenthub.application.dto.SkillOutput;
import com.agenthub.application.port.out.repositories.SkillRepository;
import com.agenthub.domain.model.Skill;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SkillUseCase {
    private final SkillRepository repository;

    public SkillOutput create(String tenantId, String workspaceId, String skillCode,
                              String name, String description, String skillType,
                              String skillPath, String skillFilesTree) {
        Skill skill = Skill.create(tenantId, workspaceId, skillCode,
                name, description, skillType, skillPath, skillFilesTree);
        return toOutput(repository.save(skill));
    }

    public SkillOutput get(String skillId) {
        return toOutput(findById(skillId));
    }

    public List<SkillOutput> list() {
        return repository.findAll().stream().map(this::toOutput).toList();
    }

    public List<SkillOutput> listByTenantAndWorkspace(String tenantId, String workspaceId) {
        return repository.findByTenantIdAndWorkspaceId(tenantId, workspaceId)
                .stream().map(this::toOutput).toList();
    }

    public SkillOutput update(String skillId, String name, String description,
                              String skillFilesTree) {
        Skill skill = findById(skillId);
        skill.update(name, description, skillFilesTree);
        return toOutput(repository.save(skill));
    }

    public SkillOutput enable(String skillId) {
        Skill skill = findById(skillId);
        skill.enable();
        return toOutput(repository.save(skill));
    }

    public SkillOutput disable(String skillId) {
        Skill skill = findById(skillId);
        skill.disable();
        return toOutput(repository.save(skill));
    }

    public void delete(String skillId) {
        findById(skillId);
        repository.deleteById(skillId);
    }

    private Skill findById(String skillId) {
        return repository.findById(skillId)
                .orElseThrow(() -> new NotFoundException("Skill not found: " + skillId));
    }

    private SkillOutput toOutput(Skill skill) {
        return new SkillOutput(skill.getId(), skill.getTenantId(), skill.getWorkspaceId(),
                skill.getSkillCode(), skill.getName(), skill.getDescription(),
                skill.getSkillType(), skill.getSkillPath(), skill.getSkillFilesTree(),
                skill.isEnabled(), skill.getCreatedAt(), skill.getUpdatedAt());
    }
}
