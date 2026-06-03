package com.agenthub.application.port.out.repositories;

import com.agenthub.domain.model.skill.Skill;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 技能仓储接口，定义技能的持久化操作。
 */
public interface SkillRepository {

    Skill saveOrUpdate(Skill skill);

    Optional<Skill> findById(String skillId);

    List<Skill> findAll();

    List<Skill> findByTenantIdAndWorkspaceId(String tenantId, String workspaceId);

    void deleteById(String skillId);

    List<Skill> findByWorkspaceId(String workspaceId);

    List<Skill> findByIds(List<String> toolIds);

    void deleteBefore(Instant minus);

    void updateFileStats(String skillId, int fileCount, long totalSize);

    void updateSyncTime(String skillId);

    void updateById(Skill skill);

    List<Skill> search(String keyword);

    Optional<Skill> findBySkillCode(String skillCode);
}
