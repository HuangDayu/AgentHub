package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.Skill;

import java.util.List;
import java.util.Optional;

/**
 * 技能仓储接口，定义技能的持久化操作。
 */
public interface SkillRepository {

    Skill save(Skill skill);

    Optional<Skill> findById(String skillId);

    List<Skill> findAll();

    List<Skill> findByTenantIdAndWorkspaceId(String tenantId, String workspaceId);

    void deleteById(String skillId);
}
