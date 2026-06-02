package com.agenthub.application.usecase;

import cn.hutool.core.bean.BeanUtil;
import com.agenthub.application.command.SkillRequestCommand;
import com.agenthub.application.dto.SkillOutput;
import com.agenthub.application.port.out.repositories.SkillRepository;
import com.agenthub.application.port.out.tools.SkillToolScannerPort;
import com.agenthub.domain.exception.NotFoundException;
import com.agenthub.domain.model.skill.Skill;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

import static com.agenthub.common.utils.TtlUtils.parallelStreamWithTtl;

@Component
@RequiredArgsConstructor
public class SkillUseCase {
    private final SkillRepository repository;
    private final SkillToolScannerPort skillToolScannerPort;

    @Value("${agenthub.skills.share-path:${user.home}/.agents/skills}")
    private String skillSharePath;

    public SkillOutput create(SkillRequestCommand command) {
        Skill skill = BeanUtil.copyProperties(command, Skill.class);
        return toOutput(repository.saveOrUpdate(skill));
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

    public SkillOutput update(String skillId, SkillRequestCommand command) {
        Skill skill = BeanUtil.copyProperties(command, Skill.class);
        skill.setId(skillId);
        return toOutput(repository.saveOrUpdate(skill));
    }

    public SkillOutput enable(String skillId) {
        Skill skill = findById(skillId);
        skill.enable();
        return toOutput(repository.saveOrUpdate(skill));
    }

    public SkillOutput disable(String skillId) {
        Skill skill = findById(skillId);
        skill.disable();
        return toOutput(repository.saveOrUpdate(skill));
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

    public void sync() {
        List<Skill> skills = skillToolScannerPort.scanSkills(skillSharePath);
        Instant now = Instant.now();
        repository.deleteBefore(now);
        parallelStreamWithTtl(4, skills, skill -> {
            skill.setUpdatedAt(now);
            repository.saveOrUpdate(skill);
            return null;
        });
    }
}
