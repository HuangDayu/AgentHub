package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.skill.SkillConfig;

import java.util.List;
import java.util.Optional;

/**
 * 技能配置仓储接口。
 */
public interface SkillConfigRepository {

    SkillConfig saveOrUpdate(SkillConfig config);

    Optional<SkillConfig> findById(String id);

    List<SkillConfig> findByTenantIdAndWorkspaceId(String tenantId, String workspaceId);

    List<SkillConfig> findAll();

    void deleteById(String id);
}
